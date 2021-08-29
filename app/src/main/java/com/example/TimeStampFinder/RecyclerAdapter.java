package com.example.TimeStampFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    Context context;
    private ArrayList<Data> listData = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return listData.size();
    }

    void addItem(Data data) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
    }


    boolean removeAll(){
        // 외부에서 모든 Item을 제거할 함수입니다.
        return listData.removeAll(listData);
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView index;
        private TextView content;
        private Button tBtn;

        ItemViewHolder(View itemView) {
            super(itemView);

            index= itemView.findViewById(R.id.textView1);
            content = itemView.findViewById(R.id.textView2);
            tBtn = itemView.findViewById(R.id.tBtn);
        }

        void onBind(Data data) {
            index.setText(data.getTitle());
            content.setText(data.getContent());
            int exsec = 30000;

            tBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // index(초단위를 index로 저장할 예정)를 받아 해당 index로 이동하게끔 버튼 설정
                    int n = Integer.parseInt(data.getTitle());
                    // msec단위 (1초=1000, index는 10초 간격) 조정
                    n *= 10000;
                    ((ConvertActivity) ConvertActivity.mContext).show(n);
                }
            });
        }
    }
}