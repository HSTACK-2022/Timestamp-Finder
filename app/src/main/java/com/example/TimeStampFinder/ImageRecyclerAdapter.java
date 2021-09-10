package com.example.TimeStampFinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<ImageRecyclerAdapter.ItemViewHolder> {

    private ArrayList<ImageData> image_listData = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.img_recycler_item, parent, false);
        return new ImageRecyclerAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(image_listData.get(position));
    }

    @Override
    public int getItemCount() {
        return image_listData.size();
    }

    void addItem(ImageData data) {
        // 외부에서 item을 추가시킬 함수입니다.
        image_listData.add(data);
    }

    boolean removeAll(){
        // 외부에서 모든 Item을 제거할 함수입니다.
        return image_listData.removeAll(image_listData);
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView index;
        private Button tBtn;

        ItemViewHolder(View itemView) {
            super(itemView);

            imageView= itemView.findViewById(R.id.image_imageView);
            tBtn = itemView.findViewById(R.id.image_tBtn);
        }

        void onBind(ImageData data) {
            imageView.setImageBitmap(data.getImage());
            tBtn.setText(data.getTitle());

            tBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // index(초단위를 index로 저장할 예정)를 받아 해당 index로 이동하게끔 버튼 설정
                    String title[] = data.getTitle().split(":");
                    int mins = Integer.parseInt(title[0]);
                    int secs = Integer.parseInt(title[1]);
                    int n = (mins*60 + secs)*1000;      // msec단위 조정
                    ((ConvertActivity) ConvertActivity.mContext).show(n);
                }
            });
        }
    }
}
