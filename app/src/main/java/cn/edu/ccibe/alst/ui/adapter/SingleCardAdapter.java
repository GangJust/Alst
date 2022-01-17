package cn.edu.ccibe.alst.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cn.edu.ccibe.alst.databinding.SingleCardListItemBinding;
import cn.edu.ccibe.alst.entity.CollapsedEntity;

public class SingleCardAdapter extends RecyclerView.Adapter<SingleCardAdapter.ViewHolder> {
    private ArrayList<CollapsedEntity> collapsedEntities;
    private SingleItemOnClickListener itemOnClickListener;

    public void setCollapsedEntities(ArrayList<CollapsedEntity> collapsedEntities) {
        this.collapsedEntities = collapsedEntities;
    }

    public void setItemOnClickListener(SingleItemOnClickListener itemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleCardListItemBinding binding = SingleCardListItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollapsedEntity collapsed = collapsedEntities.get(position);
        holder.binding.singleCardTitle.setText(collapsed.getTitle());

        // 子列表
        ArrayList<CollapsedEntity.OptionLinkEntity> optionLinkEntities = collapsed.getLinkEntities();
        SublistSingleCardAdapter sublistSingleCardAdapter = new SublistSingleCardAdapter(holder);
        if (optionLinkEntities.size() != 0) {
            sublistSingleCardAdapter.setOptionLinkEntities(optionLinkEntities);
            holder.binding.singleCardSublist.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.binding.singleCardSublist.setAdapter(sublistSingleCardAdapter);
            //holder.binding.singleCardSublist.setVisibility(View.VISIBLE);
        }

        // 点击事件
        holder.binding.getRoot().setOnClickListener(v -> {
            if (itemOnClickListener != null) {
                holder.toggleSublist();
                itemOnClickListener.onItemClick(v, collapsed, sublistSingleCardAdapter, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collapsedEntities == null ? 0 : collapsedEntities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private boolean toggle = false;
        private SingleCardListItemBinding binding;

        public ViewHolder(SingleCardListItemBinding binding) {
            this(binding.getRoot());
            this.binding = binding;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        // 切换列表显示
        private void toggleSublist() {
            toggle = !toggle;
            if (toggle) {
                binding.singleCardSublist.setVisibility(View.VISIBLE);
            } else {
                binding.singleCardSublist.setVisibility(View.GONE);
            }
        }
    }

    public interface SingleItemOnClickListener {
        void onItemClick(View view, CollapsedEntity collapsed, SublistSingleCardAdapter sublistSingleCardAdapter, int position);
    }

    /**
     * 内部 List(Recycler)
     */
    public static class SublistSingleCardAdapter extends RecyclerView.Adapter<SublistSingleCardAdapter.ViewHolder> {
        private SingleCardAdapter.ViewHolder parentHolder;

        private ArrayList<CollapsedEntity.OptionLinkEntity> optionLinkEntities;
        private SublistItemOnClickListener sublistItemOnClickListener;

        public SublistSingleCardAdapter(SingleCardAdapter.ViewHolder parentHolder) {
            this.parentHolder = parentHolder;
        }

        public void setOptionLinkEntities(ArrayList<CollapsedEntity.OptionLinkEntity> optionLinkEntities) {
            this.optionLinkEntities = optionLinkEntities;
        }

        public void setSublistItemOnClickListener(SublistItemOnClickListener sublistItemOnClickListener) {
            this.sublistItemOnClickListener = sublistItemOnClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SingleCardListItemBinding binding = SingleCardListItemBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CollapsedEntity.OptionLinkEntity optionLink = optionLinkEntities.get(position);
            holder.binding.singleCard.setCardElevation(0); //去掉卡片阴影
            holder.binding.singleCard.setRadius(0); //去掉卡片圆角

            //去掉外边距
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.binding.singleCard.getLayoutParams();
            layoutParams.bottomMargin = 0;
            layoutParams.topMargin = 0;
            holder.binding.singleCard.setLayoutParams(layoutParams);

            holder.binding.singleCardTitle.setText(optionLink.getTitle());
            holder.itemView.setOnClickListener(v -> {
                if (sublistItemOnClickListener != null) {
                    //parentHolder.toggleSublist();
                    sublistItemOnClickListener.onSublistItemClick(v, optionLink, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return optionLinkEntities == null ? 0 : optionLinkEntities.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private SingleCardListItemBinding binding;

            public ViewHolder(SingleCardListItemBinding binding) {
                this(binding.getRoot());
                this.binding = binding;
            }

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        public interface SublistItemOnClickListener {
            void onSublistItemClick(View view, CollapsedEntity.OptionLinkEntity optionLinkEntity, int position);
        }
    }
}