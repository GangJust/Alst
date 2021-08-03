package cn.edu.swsm.alst.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cn.edu.swsm.alst.databinding.SimpleListItem1Binding;
import cn.edu.swsm.alst.entity.InformEntity;


public class InformAdapter extends RecyclerView.Adapter<InformAdapter.ViewHolder> {
    private ArrayList<InformEntity> informList;
    private ItemOnClickListener itemOnClickListener;

    public void setInformList(ArrayList<InformEntity> informList) {
        this.informList = informList;
    }

    public void setItemOnClickListener(ItemOnClickListener itemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SimpleListItem1Binding binding = SimpleListItem1Binding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InformEntity inform = informList.get(position);
        holder.binding.itemText.setText(inform.getTitle());
        holder.binding.getRoot().setOnClickListener(v -> {
            if (itemOnClickListener != null) {
                itemOnClickListener.onItemClick(v, inform, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return informList == null ? 0 : informList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private SimpleListItem1Binding binding;

        public ViewHolder(SimpleListItem1Binding binding) {
            this(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface ItemOnClickListener {
        void onItemClick(View view, InformEntity inform, int position);
    }
}
