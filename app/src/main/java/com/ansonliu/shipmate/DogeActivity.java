package com.ansonliu.shipmate;

import android.content.Context;
import android.graphics.Interpolator;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DogeActivity extends AppCompatActivity {

    interface Shard {
        void shardToAngle(double angle, int count);
    }

    RelativeLayout linearLayout;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doge);

        linearLayout = (RelativeLayout) findViewById(R.id.dogeLayout);

        final ImageView bomb = new ImageView(this);
        bomb.setImageResource(R.drawable.ic_play_dark);
        bomb.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(bomb);


        AnimationSet bombDropSet = new AnimationSet(false);
        bombDropSet.setFillAfter(true);

        // create translation animation for bomb
        TranslateAnimation transBomb = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
                0.5f, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0.9f);
        transBomb.setDuration(2000);
        ScaleAnimation scaleBomb = new ScaleAnimation(1.0f, .6f, 1.0f, 1.0f, 0.5f, 0.5f);
        scaleBomb.setDuration(2000);

        // add new animations to the set
        bombDropSet.addAnimation(scaleBomb);
        bombDropSet.addAnimation(transBomb);

        final AnimationSet bombMobe = new AnimationSet(false);
        bombMobe.setFillAfter(true);
        bombMobe.setFillEnabled(true);
        bombMobe.setFillBefore(false);

        bombDropSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shardExplode();

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setDuration(1000);
                fadeOut.setFillAfter(true);
                bomb.startAnimation(fadeOut);



                /*
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Thread.sleep(3000);// change the time according to your need
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        shardExplode();
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("T", e.toString());
                            }
                        }
                    }
                }).start();
                */
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        bomb.startAnimation(bombDropSet);
    }

    public void shardExplode() {
        int count = 0;

        Shard shardImpl = new Shard() {
            @Override
            public void shardToAngle(double angle, int count) {
                Context mContext = getApplicationContext();
                final ImageView shard = new ImageView(mContext);
                shard.setImageResource(R.drawable.ic_play_dark);
                shard.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(shard);

                final AnimationSet shardEffects = new AnimationSet(false);
                shardEffects.setFillAfter(true);

                double randomAngle = (angle*Math.PI)/180;

                float yDelta = 0.9f; //we change this to -0.9f after calculating xDelta
                float xDelta = (float)(yDelta/Math.tan(randomAngle));
                yDelta *= -1;

                if (xDelta == Float.POSITIVE_INFINITY)
                    xDelta = 0.9f;
                else if (xDelta == Float.NEGATIVE_INFINITY)
                    xDelta = -0.9f;

                Log.e("P", Double.toString(randomAngle)+ " " + Float.toString(xDelta) + " " + Float.toString(yDelta));

                TranslateAnimation moveShard = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.5f,
                        Animation.RELATIVE_TO_PARENT, 0.5f+xDelta,
                        Animation.RELATIVE_TO_PARENT, 0.9f,
                        Animation.RELATIVE_TO_PARENT, yDelta);
                //moveShard.setDuration((long) ((0.7071f/Math.sqrt(xDelta*xDelta+yDelta*yDelta)) * 3000 * ((Math.random()%6+7)/10)*2));
                //moveShard.setDuration((long) ((Math.sqrt(xDelta*xDelta+yDelta*yDelta)/(0.6*Math.sqrt(2))) * .5 * 3000));
                long scaledDuration = (long) (Math.random() * 3 * 5000)-(count*10);
                if (scaledDuration < 500) {
                    scaledDuration = (long) (Math.random() * 10 * 500);
                }
                moveShard.setDuration(scaledDuration);

                RotateAnimation rotateShard = new RotateAnimation(0, 360, 0.5f, 0.5f);
                rotateShard.setDuration(7000);

                //Log.e("PEPIN", Float.toString((float)i * (1.0f / 50f)));
                //Log.e("PEPIN", Long.toString((long) ((0.8f/Math.sqrt(xDelta*xDelta+yDelta*yDelta)) * 3000)));

                // add new animations to the set
                shardEffects.addAnimation(rotateShard);
                shardEffects.addAnimation(moveShard);

                shardEffects.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        /* http://stackoverflow.com/a/4295570/761902 */
                        linearLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                shard.setVisibility(ImageView.GONE);
                                linearLayout.removeView(shard);
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                shard.startAnimation(shardEffects);
            }
        };

        /*
        for (int i = 0; i < 18; i = i+1) {
            shardImpl.shardToAngle(i*10, count);
        }
        */
        for (int i = 0; i < 300; i = i+1) {
            shardImpl.shardToAngle(Math.random() * 180, count);
            count++;
        }
    }
}
