/*
 * Copyright (C) 2015  Andrew Gunnerson <andrewgunnerson@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.chenxiaolong.dualbootpatcher.switcher.service;

import android.content.Context;
import android.util.Log;

import com.github.chenxiaolong.dualbootpatcher.socket.MbtoolSocket;
import com.github.chenxiaolong.dualbootpatcher.socket.MbtoolSocket.SetKernelResult;

import java.io.IOException;

public final class SetKernelTask extends BaseServiceTask {
    private static final String TAG = SetKernelTask.class.getSimpleName();

    public final String mRomId;
    private final SetKernelTaskListener mListener;

    public SetKernelResult mResult;

    public interface SetKernelTaskListener extends BaseServiceTaskListener {
        void onSetKernel(int taskId, String romId, SetKernelResult result);
    }

    public SetKernelTask(int taskId, Context context, String romId, SetKernelTaskListener listener) {
        super(taskId, context);
        mRomId = romId;
        mListener = listener;
    }

    @Override
    public void execute() {
        Log.d(TAG, "Setting kernel for " + mRomId);

        mResult = SetKernelResult.FAILED;
        try {
            mResult = MbtoolSocket.getInstance().setKernel(getContext(), mRomId);
        } catch (IOException e) {
            Log.e(TAG, "mbtool communication error", e);
        }

        mListener.onSetKernel(getTaskId(), mRomId, mResult);
    }
}
