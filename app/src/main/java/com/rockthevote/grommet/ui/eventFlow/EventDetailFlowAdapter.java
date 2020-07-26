package com.rockthevote.grommet.ui.eventFlow;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Mechanical Man, LLC on 7/17/17. Grommet
 */

public class EventDetailFlowAdapter extends PagerAdapter {
    private Context context;
    private ArrayMap<Integer, EventFlowPage> pages = new ArrayMap<>();

    public EventDetailFlowAdapter(Context context) {
        this.context = context;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        View view;
        switch (position) {
            case 0:
                view = new EventPartnerLogin(context);
                break;
            case 1:
                view = new EventCanvasserInfo(context);
                break;
            case 2:
                view = new SessionTimeTracking(context);
                break;
            case 3:
                view = new SessionSummary(context);
                break;
            default:
                view = new EventCanvasserInfo(context);
                break;
        }

        collection.addView(view);
        pages.put(position,(EventFlowPage) view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
        pages.remove(view);
    }

    public EventFlowPage getPageAtPosition(int position) {
        return pages.get(position);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
