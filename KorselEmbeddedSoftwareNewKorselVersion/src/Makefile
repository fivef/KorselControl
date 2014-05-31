## General Flags
SRCDIR = src
OBJDIR = obj
CPU = attiny2313

SRCS = $(SRCDIR)/Korsel_BT.c \
	#$(SRCDIR)/V24Control.c \

OBJS =  $(SRCDIR)/Korsel_BT.o  \
	#$(SRCDIR)/V24Control.o \

TARGET = bin/Korsel_BT
TARGET_HEX = bin/Korsel_BT.hex

COMPILER = avr-g++
LINKER = avr-g++


CXXFLAGS = -mmcu=${CPU} -I/usr/lib/avr/include -Os

LIBS = -lm

LFLAGS =  -Wl,-Map,avr.map 




first: all

all: debug

debug: 	$(OBJS)
	mkdir -p obj/
	mv $(OBJS) $(OBJDIR)/
	$(LINKER) $(CXXFLAGS) $(LFLAGS) -o $(TARGET) $(OBJDIR)/*.o $(LIBS)
	avr-strip $(TARGET)
	avr-size --format=sysv $(TARGET)



release: debug
	
	./avr_upload


$(OBJS):  
	$(COMPILER) -c $(CXXFLAGS) $(INCPATH) -o $@ $(@:.o=.c)




bla:	
	avr-gcc -mmcu=$(MCU) -I/usr/avr/include/avr -Os -Wl,-Map,Korsel_02.map -o $(TARGET) *.o -lm
	avr-strip $(TARGET)
	avr-size --format=sysv $(TARGET)

test:
	avr-gcc -c -mmcu=$(MCU) -I/usr/avr/include/avr -Os -o Korsel_02.o ../Korsel_02.c



clean: 
	rm -f *~ $(OBJS) $(TARGET) $(TARGET_HEX) $(SRCDIR)/*~ $(OBJDIR)/*.o


