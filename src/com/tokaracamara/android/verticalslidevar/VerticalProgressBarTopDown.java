package com.tokaracamara.android.verticalslidevar;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;





public class VerticalProgressBarTopDown extends VerticalProgressBar {

	public VerticalProgressBarTopDown(Context context) {
	    	super(context, null);
	    }

	public VerticalProgressBarTopDown(Context context, AttributeSet attrs) {
	        super(context, attrs, android.R.attr.progressBarStyle);
	    }
	
	public VerticalProgressBarTopDown(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
	}

	 @Override
	    protected synchronized void onDraw(Canvas canvas) {

	    	super.onDraw(canvas);

	        Drawable d = super.getCurrentDrawable();
	        if (d != null) {
	            // Translate canvas so a indeterminate circular progress bar with padding
	            // rotates properly in its animation
	            canvas.save();
	            float py = this.getHeight()/2.0f;
	            float px = this.getWidth()/2.0f;
	            canvas.rotate(180, px, py);
	            canvas.translate(mPaddingLeft, mPaddingTop);
	           // canvas.translate(20, 10);
	            d.draw(canvas);
	            canvas.restore();
	        }
	    }


}
