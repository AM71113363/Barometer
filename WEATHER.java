package sss.am71113363.barometer;

import android.view.*;
import android.graphics.*;
import android.content.Context;
import android.util.*;
//import java.util.*;


public class WEATHER extends View
{
    private Bitmap bm;
    private Bitmap br;
    private Paint paint;
    private Matrix matrix=new Matrix();
    private float hPa=0.0f;
    private String altitude="0m";
    private float adjustAltitude=0;
	
    private float getAngle()
    {
        float pr=hPa + adjustAltitude;
	if(pr<=990){ return -90; }
	if(pr>=1020){ return 90; }
	//range 990 to 1020
	pr=(pr-990) * 6;
        pr-=90;
      return pr;
    }
    public WEATHER(Context ctx, AttributeSet attr)
    {
        super(ctx,attr);
	bm=BitmapFactory.decodeResource(getResources(),R.drawable.back);
	br=BitmapFactory.decodeResource(getResources(),R.drawable.arm);
	paint=new Paint();
	paint.setColor(Color.WHITE);
	paint.setTextSize(50);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        if(bm==null)
	{
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}else{
	    setMeasuredDimension(bm.getWidth(),bm.getHeight());
	}
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(bm!=null)
	{
	    canvas.drawBitmap(bm,0,0,null);
        }
	canvas.drawText(String.format("%.2f", hPa),170,370,paint);
	canvas.drawText(altitude,170,440,paint);

	if(br!=null && hPa > 0.0f)
	{
	    matrix.reset();
	    matrix.postTranslate(-br.getWidth()/2f,-br.getHeight()/2f);
	    matrix.postRotate(getAngle(),0,0);
	    matrix.postTranslate(br.getWidth()/2,br.getHeight()/2);
	    canvas.drawBitmap(br,matrix,null);
	}
    }
    public void update(float hpa)
    {
        hPa = hpa;
	invalidate();
    }
    public void update(float a,String s)
    {
        altitude=s;
	adjustAltitude = a/8.3f;
    }
}
