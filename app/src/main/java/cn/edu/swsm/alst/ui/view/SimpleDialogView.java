package cn.edu.swsm.alst.ui.view;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cn.edu.swsm.alst.R;
import cn.edu.swsm.alst.databinding.SimpleDialogLayoutBinding;

public class SimpleDialogView extends DialogFragment {
    private SimpleDialogLayoutBinding binding;

    private AgreeOnClickListener agreeOnClickListener;
    private CancelCOnClickListener cancelCOnClickListener;
    private OnDismissListener onDismissListener;

    private CharSequence title;
    private CharSequence contentText;
    private View dialogContentView;
    private CharSequence agreeText;
    private CharSequence cancelText;

    public SimpleDialogView setAgreeOnClickListener(AgreeOnClickListener agreeOnClickListener) {
        this.agreeOnClickListener = agreeOnClickListener;
        return this;
    }

    public void setCancelCOnClickListener(CancelCOnClickListener cancelCOnClickListener) {
        this.cancelCOnClickListener = cancelCOnClickListener;
    }

    public SimpleDialogView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SimpleDialogLayoutBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.simpleDialogTitle.setText(title);
        Resources resources = view.getContext().getResources();

        // 内容
        if (dialogContentView != null) {
            binding.simpleDialogContent.addView(dialogContentView);
        } else if (contentText != null) {
            TextView textView = new TextView(view.getContext());
            textView.setText(contentText);
            textView.setTextColor(resources.getColor(R.color.subtitle_color));
            binding.simpleDialogContent.addView(textView);
        }

        // 同意按钮文本
        binding.simpleDialogAgree.setText(agreeText);


        // 取消按钮文本
        if (cancelText != null) {
            binding.simpleDialogCancel.setVisibility(View.VISIBLE);
            binding.simpleDialogCancel.setText(cancelText);
        }

        // 同意点击事件
        binding.simpleDialogAgree.setOnClickListener(v -> {
            if (agreeOnClickListener != null)
                agreeOnClickListener.onAgreeClick(this);
        });

        // 取消点击事件
        binding.simpleDialogCancel.setOnClickListener(v -> {
            if (cancelCOnClickListener != null)
                cancelCOnClickListener.onCancelClick(this);
        });

        //window 背景透明，使用自定义背景
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public SimpleDialogView setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public SimpleDialogView setContentText(CharSequence contentText) {
        this.contentText = contentText;
        return this;
    }

    public void setDialogContentView(View dialogContentView) {
        this.dialogContentView = dialogContentView;
    }

    public SimpleDialogView setAgreeText(CharSequence agreeText) {
        this.agreeText = agreeText;
        return this;
    }

    public SimpleDialogView setCancelText(CharSequence cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public SimpleDialogView setCancelableDismiss(boolean cancelable) {
        this.setCancelable(cancelable);
        return this;
    }

    @Override
    public void dismiss() {
        if (onDismissListener != null) onDismissListener.onDismissCallBack();
        super.dismiss();
    }

    public interface AgreeOnClickListener {
        void onAgreeClick(SimpleDialogView dialog);
    }

    public interface CancelCOnClickListener {
        void onCancelClick(SimpleDialogView dialog);
    }

    public interface OnDismissListener {
        void onDismissCallBack();
    }

}
