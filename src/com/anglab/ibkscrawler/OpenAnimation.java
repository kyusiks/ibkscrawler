/*  Created by Edward Akoto on 12/31/12.
 *  Email akotoe@aua.ac.ke
 * 	Free for modification and distribution
 */

package com.anglab.ibkscrawler;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class OpenAnimation extends TranslateAnimation implements
		Animation.AnimationListener {

	public OpenAnimation(LinearLayout layout, int fromXType,
			float fromXValue, int toXType, float toXValue, int fromYType,
			float fromYValue, int toYType, float toYValue) {

		super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue);

		setDuration(250);
		setFillAfter(true);
		setInterpolator(new AccelerateDecelerateInterpolator());
		setAnimationListener(this);
		layout.startAnimation(this);
	}

	public void onAnimationEnd(Animation arg0) {}
	public void onAnimationRepeat(Animation arg0) {}
	public void onAnimationStart(Animation arg0) {}
}