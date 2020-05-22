package polar.com.androidblesdk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;

public class StateBroadcastingVideoView extends VideoView {
    public interface PlayPauseListener{
        void onPlay();
        void onPause();
    }
    private PlayPauseListener mListener;

    public StateBroadcastingVideoView(Context context){
        super(context);
    }

    public StateBroadcastingVideoView(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    @Override
    public void pause(){
        super.pause();
        if(mListener!=null){
            mListener.onPause();
        }
    }

    public void start(){
        super.start();
        if(mListener!=null){
            mListener.onPlay();
        }
    }
    public void setPlayPauseListener(PlayPauseListener listener){
        mListener=listener;
    }
}
