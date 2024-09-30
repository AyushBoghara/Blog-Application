package com.example.blog.InterFace;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.blog.Models.ArticleModel;
import com.example.blog.Models.BookMarkModel;

import java.util.ArrayList;

public interface OnItemClickListener {

    void onItemClick(ArrayList<ArticleModel> articleModels,int position);

    void onItemClickBookMark(ArrayList<BookMarkModel> bookMarkModels,int position);
}
