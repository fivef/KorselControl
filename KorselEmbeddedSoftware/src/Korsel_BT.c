#include <avr/io.h>
#include <avr/interrupt.h>
#include "v24_commands.h"
/**
*Define CPU frequency
**/
#define F_CPU 4000000UL
#include <util/delay.h>

/**
*Debug Mode
**/
//#define debug

#define USART_BAUD_RATE 9600

/**
*Calculate and define UBRR.
**/
#define V24_UBRR (F_CPU/(USART_BAUD_RATE*8L)-1)   //UCSRA U2X=1

#define PWM_motor_left		OCR1A
#define PWM_motor_right		OCR1B

#define motor_right_direction	PB1
#define motor_left_direction	PB2

#define RXD			PD0
#define TXD			PD1
#define photo_sensor 		PD2
#define button_top 		PD3
#define button_left		PD4
#define button_right		PD5
#define button_middle		PD6

/**
*true if last package received was the protocol header LEFT_MOTOR_SPEED_FORWARD
**/
bool LEFT_MOTOR_SPEED_FORWARD_headerDetected = false;
/**
*true if last package received was the protocol header LEFT_MOTOR_SPEED_BACKWARD
**/
bool LEFT_MOTOR_SPEED_BACKWARD_headerDetected = false;
/**
*true if last package received was the protocol header RIGHT_MOTOR_SPEED_FORWARD
**/
bool RIGHT_MOTOR_SPEED_FORWARD_headerDetected = false;
/**
*true if last package received was the protocol header RIGHT_MOTOR_SPEED_BACKWARD
**/
bool RIGHT_MOTOR_SPEED_BACKWARD_headerDetected = false;

/**
*Command struct consisting of header and data
**/
typedef struct V24Command {
	unsigned char command;
	char data;
} V24COMMAND;

/**
*initialize IO ports
**/
void init_io(void)
{
								//PWM motor left (OC1B)
	DDRB |= (1 << DDB4);		//set PB4 as output
	PORTB &= ~(1 << PB4);		//set PB4 low

								//PWM motor right (OC1A)
	DDRB |= (1 << DDB3);		//set PB3 as output
	PORTB &= ~(1 << PB3);		//set PB3 auf low

								//direction motor left							
	DDRB |= (1 << DDB2);		//set PB2 as output
	PORTB &= ~(1 << PB2);		//set PB2 as low

								//direction motor right						
	DDRB |= (1 << DDB1);		//set PB1 as output
	PORTB &= ~(1 << PB1);		//set PB1 as low


								//button middle
	DDRD &= ~(1 << DDD6);		//set PD6 as input
	PORTD |= (1 << PD6);		//set PD6 as input with Pull-UP


								//button right
	DDRD &= ~(1 << DDD5);		//set PD5 as input
	PORTD |= (1 << PD5);		//set PD5 as input witht Pull-UP


								//button left
	DDRD &= ~(1 << DDD4);		//set PD4 as input
	PORTD |= (1 << PD4);		//set PD4 as input with Pull-UP


								//button top
	DDRD &= ~(1 << DDD3);		//set PD3 as input
	PORTD |= (1 << PD3);		//set PD3 as input with Pull-UP


								//photo sensor
	DDRD &= ~(1 << DDD2);		//set PD2 as input
	PORTD |= (1 << PD2);		//set PD2 as input with Pull-UP


								//TXD BT_MODUL
	DDRD |= (1 << DDD1);		//set PD1 as output

								//RXD BT_MODUL
	DDRD &= ~(1 << DDD0);		//set PD2 as input

}

/**
*delay of 100 ms
**/
void delay (void)
{
	_delay_ms(100);
}

/**
*Initialize motor outputs as PWM  (pulse width modulation)
**/
void pwm (void)
{
	TCCR1B |= (1 << CS11);					//system clock
	TCCR1B &= ~((1 << WGM12) | (1 << WGM13));		//standard PWM


	TCCR1A |= (1 << WGM10);					//8 bit PWM
	TCCR1A &= ~(0 << WGM11);


	TCCR1A |= (1 << COM1A1) | (1 << COM1B1);		//PWM output not inverted

	PWM_motor_left = 0;	//set speed 0
	PWM_motor_right = 0;  
}

/**
*Initialize interrupts
**/
void setup_interrupt(void)

{
    	PCMSK = (1<<photo_sensor);//Un-mask photo_sensor

	//Any logical change on INT0 generates an interrupt request
	MCUCR |= (1<<ISC00); 
	MCUCR &= ~(1<<ISC01);

    	GIMSK = (1<<INT0);//Enable the PORTB pin change interrupt

}

/**
*Initialize USART for serial communication.
*
*no parity, 1 stop bit, char size 8
**/
void USART_Init()
{
	/* Set baud rate */
	UBRRH = (unsigned char)(V24_UBRR>>8);
	UBRRL = (unsigned char)V24_UBRR;

	/* Enable receiver,transmitter and Receive Complete Interrupt*/
	UCSRB = (1<<RXEN)|(1<<TXEN)|(1<<RXCIE);

	//Asynchronous Double Speed mode
	UCSRA |= (1<<U2X);

	/*Set Frame Format

	No Parity  (in UCSRC UMP0 und UMP1 = 0)
	1 StopBit  (in UCSRC USBS = 0)
	char size 8 (in UCSRC UCSZ1 and UCSZ0 = 1, in UCSRB UCSZ2 = 0)

	*/

	UCSRC = (1 << UCSZ1)|(1 << UCSZ0);
}

/**
*Transmit a char on USART
**/
void USART_Transmit( unsigned char data )
{
	/* Wait for empty transmit buffer */
	while ( !( UCSRA & (1<<UDRE)) )
	;
	/* Put data into buffer, sends the data */
	UDR = data;

	
	// wait until transmission is complete
	while (!(UCSRA & (1<<TXC)));
}

/**
*Transmit a package consisting of a header and a value on USART
**/
void USART_Transmit_Command(V24COMMAND* command)
{
	USART_Transmit(command->command);
	USART_Transmit(command->data);
}

/**
*If a contact button is pressed this method is called
*Resets motor speed to 0 and sends a "button pressed" command
**/
void Stop_Motor(){

	PWM_motor_right = 0; 
	PWM_motor_left = 0;

	V24COMMAND *command;
	command->command = BUTTON_PRESSED;
	command->data = 1;	

	USART_Transmit_Command(command);

	delay();
	delay();
	delay();
	delay();

}

/**
*ISR for photo sensor.
*
*this ISR is called if the photo sensor state changes from white to black or from black to white
**/
ISR(INT0_vect)
{
    	//USART_Transmit('M');
	//PWM_motor_right = 254;
	//PWM_motor_left = 254; 
	V24COMMAND *command;
	command->command = PHOTO_SENSOR;

	if(PIND & (1 << photo_sensor)){	
		command->data = 1;
	}else{
		command->data = 0;
	}

	USART_Transmit_Command(command);
}

/**
*ISR for USART receive.
*
*A command consists of two chars. The first one ist the header and the second one the speed value.
*The first time the ISR is called the char is read from the UDR. If a header was detected the headerDetected Flag is set.
*The next time the ISR is called the char read from UDR is a value for motor speed.
**/
ISR(USART_RX_vect){
	
	char readValue = UDR;
	
	//LEFT MOTOR SPEED VALUE if previous char was a header

	if(LEFT_MOTOR_SPEED_FORWARD_headerDetected == true){

		LEFT_MOTOR_SPEED_FORWARD_headerDetected = false;

		PWM_motor_left = readValue;

		PORTB &= ~(1 << motor_left_direction);

	}

	if(LEFT_MOTOR_SPEED_BACKWARD_headerDetected == true){

		LEFT_MOTOR_SPEED_BACKWARD_headerDetected = false;

		PWM_motor_left = readValue;

		PORTB |= (1 << motor_left_direction);
	}

	//RIGHT MOTOR SPEED VALUE if previous char was a header

	if(RIGHT_MOTOR_SPEED_FORWARD_headerDetected == true){

		RIGHT_MOTOR_SPEED_FORWARD_headerDetected = false;

		PWM_motor_right = readValue;

		PORTB |= (1 << motor_right_direction);
		
		
	}

	if(RIGHT_MOTOR_SPEED_BACKWARD_headerDetected == true){

		RIGHT_MOTOR_SPEED_BACKWARD_headerDetected = false;

		PWM_motor_right = readValue;

		
		PORTB &= ~(1 << motor_right_direction);
	}


	//detect headers

	//LEFT MOTOR HEADER

	if(readValue == LEFT_MOTOR_SPEED_FORWARD){

		LEFT_MOTOR_SPEED_FORWARD_headerDetected = true;
	}

	if(readValue == LEFT_MOTOR_SPEED_BACKWARD){

		LEFT_MOTOR_SPEED_BACKWARD_headerDetected = true;	
	}

	//RIGHT MOTOR HEADER
	if(readValue == RIGHT_MOTOR_SPEED_FORWARD){

		RIGHT_MOTOR_SPEED_FORWARD_headerDetected = true;
	}

	if(readValue == RIGHT_MOTOR_SPEED_BACKWARD){

		RIGHT_MOTOR_SPEED_BACKWARD_headerDetected = true;	
	}
}

void diag(void){

	PWM_motor_left = 254; 
	PWM_motor_right = 254;

	PORTB |= (1 << motor_right_direction);
	PORTB |= (1 << motor_left_direction);

	delay();
	delay();

	delay();

	delay();

	delay();

	PORTB &= ~(1 << motor_left_direction);
	PORTB &= ~(1 << motor_right_direction);

	delay();
	delay();

	delay();

	delay();

	delay();

	PWM_motor_left = 0; 
	PWM_motor_right = 0;
}

/**
*Main function.
*
*Initializations and main loop
**/
int main (void)
{

init_io();		//init ports
pwm();			//init motor outputs as PWM
USART_Init();		//init USART
setup_interrupt();	//init interrupts
sei();			//globally enable interrupts







//Main loop
while (1)
{

	if(!(PIND & (1 << button_middle))){

		Stop_Motor();

	}
	
	if(!(PIND & (1 << button_right))){

		Stop_Motor();

	}
	
	if(!(PIND & (1 << button_left))){

		
		Stop_Motor();
	
	}
	
	//Diag button: full speed forward, full speed backward, stop
	if(!(PIND & (1 << button_top))){


		diag();
	
	}

}//while ende

return (0);
}
