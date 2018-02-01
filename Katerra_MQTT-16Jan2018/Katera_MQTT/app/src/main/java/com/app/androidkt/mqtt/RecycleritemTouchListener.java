package com.app.androidkt.mqtt;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by naveen on 7/16/2017.
 */
public class RecycleritemTouchListener implements RecyclerView.OnItemTouchListener{

	    private ClickListener clicklistener;
	    private GestureDetector gestureDetector;

	    public RecycleritemTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

		this.clicklistener=clicklistener;
		gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
		    @Override
		    public boolean onSingleTapUp(MotionEvent e) {
		        return true;
		    }

		    @Override
		    public void onLongPress(MotionEvent e) {
		        View child=recycleView.findChildViewUnder(e.getX(),e.getY());
		        if(child!=null && clicklistener!=null){
		            clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
		        }
		    }

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				View child=recycleView.findChildViewUnder(e.getX(),e.getY());
				if(child!=null && clicklistener!=null){
					clicklistener.onDoubleTap(child,recycleView.getChildAdapterPosition(child));
				}

				return super.onDoubleTap(e);
			}
		});
	    }

	    @Override
	    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		View child=rv.findChildViewUnder(e.getX(),e.getY());
		if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
		    clicklistener.onClick(child,rv.getChildAdapterPosition(child));
		}

		return false;
	    }

	    @Override
	    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

	    }

	    @Override
	    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

	    }
}
