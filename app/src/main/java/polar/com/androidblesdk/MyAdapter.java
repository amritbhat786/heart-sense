package polar.com.androidblesdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<VideoHolder> {

    private Context context;
    ArrayList<File> videoArrayList;
    ArrayList<File> sensorTextFileArrayList;

    public MyAdapter(Context context, ArrayList<File> videoArrayList, ArrayList<File> sensorTextFileArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
        this.sensorTextFileArrayList = sensorTextFileArrayList;
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_item,viewGroup,false);
        return new VideoHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoHolder holder, final int position) {
        if(videoArrayList.size()!=0) {
//        holder.txtFileName.setText(VideoListerActivity.videoFileArrayList.get(position).getName());
            holder.txtFileName.setText(Integer.toString(position));
            Bitmap bitmapThumbnail = ThumbnailUtils.createVideoThumbnail(videoArrayList.get(position).getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
            holder.imageThumbnail.setImageBitmap(bitmapThumbnail);

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent intent = new Intent(context, vplayerActivity.class);
                                                        intent.putExtra("position", holder.getAdapterPosition());
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        context.startActivity(intent);
                                                    }
                                                });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File deleteVideo = videoArrayList.get(position);
                    File deleteText = sensorTextFileArrayList.get(position);
                    File parentDirectory = deleteVideo.getParentFile();

                    if (deleteVideo.delete()) {
                        System.out.println("Deleted the file: " + deleteVideo.getName());
                    } else {
                        System.out.println("Failed to delete the file.");
                    }
                    if (deleteText.delete()) {
                        System.out.println("Deleted the file: " + deleteVideo.getName());
                    } else {
                        System.out.println("Failed to delete the file.");
                    }
                    if (parentDirectory.delete()) {
                        System.out.println("Directory to be deleted: "+parentDirectory);
                    } else {
                        System.out.println("Failed to delete the file.");
                    }
                    Intent intent = new Intent(context,VideoListerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    context.startActivity(intent);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        if(videoArrayList.size()>0){
            return videoArrayList.size();
        }
        else
                return 1;
    }
}

class VideoHolder extends RecyclerView.ViewHolder{

    TextView txtFileName;
    ImageView imageThumbnail;
    CardView mCardView;
    Button deleteButton;


    VideoHolder(View view){
        super(view);
        txtFileName = view.findViewById(R.id.txt_videoFileName);
        imageThumbnail= view.findViewById(R.id.iv_thmnail);
        mCardView = view.findViewById(R.id.myCardview);
        deleteButton = view.findViewById(R.id.delete_btn);
    }
}