/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.example.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.example.Images;
import com.example.cache.ImageCache.ImageCacheParams;
import com.example.cache.ImageWorker;

public class ImageDetailActivity extends FragmentActivity {
	private static final String IMAGE_CACHE_DIR = "images";
	public static final String EXTRA_IMAGE = "extra_image";
	private final static String TAG = "ImageDetailActivity";

	private ImagePagerAdapter mAdapter;
	private ImageWorker mImageWorker;
	private ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);

		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		final DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		final int height = displaymetrics.heightPixels;
		final int width = displaymetrics.widthPixels;

		ImageCacheParams cacheParams = new ImageCacheParams();
		cacheParams.reqHeight = height;
		cacheParams.reqWidth = width;
		cacheParams.memoryCacheEnabled = false;
		cacheParams.loadingResId = R.drawable.ic_launcher;
		mImageWorker = ImageWorker.newInstance(this);
		mImageWorker.addParams(TAG, cacheParams);
		// mImageWorker.setLoadingImage(R.drawable.empty_photo);

		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				Images.imageUrls.length);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		// Set the current item based on the extra passed in to this activity
		final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
		if (extraCurrentItem != -1) {
			mPager.setCurrentItem(extraCurrentItem);

		}

	}

	@Override
	public void onResume() {
		super.onResume();
		mImageWorker.setOnScreen(TAG, true);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageWorker.setOnScreen(TAG, false);
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageWorker
	 * 
	 * @return
	 */
	public ImageWorker getImageWorker() {
		return mImageWorker;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
			return ImageDetailFragment.newInstance(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			final ImageDetailFragment fragment = (ImageDetailFragment) object;
			// As the item gets destroyed we try and cancel any existing work.
			fragment.cancelWork();
			super.destroyItem(container, position, object);
		}
	}

}
