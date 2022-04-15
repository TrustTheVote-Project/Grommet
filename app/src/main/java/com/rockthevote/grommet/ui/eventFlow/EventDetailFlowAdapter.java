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
                view = new EventSplash(context);
                break;
            case 1:
                view = new EventPartnerLogin(context);
                break;
            case 2:
                view = new EventCanvasserInfo(context);
                break;
//            case 3:
//                view = new SessionTimeTracking(context);
//                break;
//            case 4:
//                view = new SessionSummary(context);
//                break;
            case 3:
                view = new EventStartCollection(context);
                break;
            case 4:
                view = new EventCollectionStatus(context);
                break;
            case 5:
                view = new EventEndStrangerCollection(context);
                break;
            case 6:
                view = new EventEndCollection(context);
                break;
            case 7:
                view = new EventEndShift(context);
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
        return 8;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
