package cn.edu.ccibe.alst.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.edu.ccibe.alst.databinding.GridCardListItemBinding;
import cn.edu.ccibe.alst.entity.GridCardEntity;

public class GridCardAdapter extends RecyclerView.Adapter<GridCardAdapter.ViewHolder> {
    private List<GridCardEntity> gridCardEntities;

    public void setGridCardEntities(List<GridCardEntity> gridCardEntities) {
        this.gridCardEntities = gridCardEntities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GridCardListItemBinding binding = GridCardListItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridCardEntity entity = gridCardEntities.get(position);
        if (entity.getIcon() != null) holder.binding.gridCardIcon.setImageBitmap(entity.getIcon());
        holder.binding.gridCardTitle.setText(entity.getTitle());
    }

    @Override
    public int getItemCount() {
        return gridCardEntities == null ? 0 : gridCardEntities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private GridCardListItemBinding binding;

        public ViewHolder(GridCardListItemBinding binding) {
            this(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}