package com.archermind.music.loadingview.indicators;


import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

/**
 */
public class AudioEqualizerIndicator extends LineScaleIndicator {


    @Override
    public void draw(Canvas canvas, Paint paint) {
        float translateX=getWidth()/11;
        float translateY=getHeight()/2;

        for (int i = 0; i < 5; i++) {
            canvas.save();
            canvas.translate((2 + i * 2) * translateX - translateX / 2, translateY);
            RectF rectF=new RectF(-translateX/2,-getHeight()/2.5f,translateX/2,getHeight()/2.5f);
            canvas.scale(SCALE, scaleYFloats[i],0f,rectF.bottom);
            canvas.drawRect(rectF, paint);
            canvas.restore();
        }
    }


    @Override
    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> animators=new ArrayList<>();
        long[] delays=new long[]{500,250,0,100,600};
        for (int i = 0; i < 5; i++) {
            final int index=i;
            ValueAnimator scaleAnim=ValueAnimator.ofFloat(1,0.3f,1);
            scaleAnim.setDuration(1000);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);
            addUpdateListener(scaleAnim,new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleYFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }

}
