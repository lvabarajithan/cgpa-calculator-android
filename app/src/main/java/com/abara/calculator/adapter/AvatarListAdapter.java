/*
 * Copyright (C) 2016 Abarajithan Lv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abara.calculator.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abara.calculator.R;
import com.abara.calculator.util.OnAvatarClickListener;
import com.abara.calculator.util.Utils;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by abara on 7/11/15.
 *
 * Adapter to populate the avatar list
 *
 */
public class AvatarListAdapter extends RecyclerView.Adapter<AvatarHolder> {

    private Context context;
    private int[] avatars = Utils.avatars;
    private OnAvatarClickListener avatarClickListener;

    public AvatarListAdapter(Context context, OnAvatarClickListener avatarClickListener) {
        this.context = context;
        this.avatarClickListener = avatarClickListener;
    }

    @Override
    public AvatarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_avatar_list_dialog, parent, false);
        return new AvatarHolder(view);
    }

    @Override
    public void onBindViewHolder(final AvatarHolder holder, int position) {

        Glide.with(context).load(avatars[position]).fitCenter().into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarClickListener.onAvatarClick(holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return avatars.length;
    }
}

/**
 * Avatar list view holder
 */
class AvatarHolder extends RecyclerView.ViewHolder {

    CircleImageView imageView;

    AvatarHolder(View itemView) {
        super(itemView);

        imageView = (CircleImageView) itemView.findViewById(R.id.avatar_circle_image_dialog);

    }
}