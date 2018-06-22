package com.example.administrator.bookdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class BookListActivity extends AppCompatActivity {
    private static final String TAG = "BookListActivity";
    private ListView mListView;
    private List<BookListResult.DataBean> mBook;
    private AsyncHttpClient mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        String url = "http://www.imooc.com/api/teacher?type=10";
        mListView = findViewById(R.id.book_list_view);
        requestData(url);
    }

    private void requestData(String url) {
        mClient = new AsyncHttpClient();

        mClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                final String result = new String(responseBody);
                Log.i(TAG, "onSuccess: " + result);
                BookListResult bookListResult =
                        new Gson().fromJson(result, BookListResult.class);
                mBook = bookListResult.getData();

                mListView.setAdapter(new BookListAdapter());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, BookListActivity.class);
        context.startActivity(intent);
    }

    private class BookListAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mBook == null ? 0 : mBook.size();
        }

        @Override
        public Object getItem(int position) {
            return mBook.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final BookListResult.DataBean book = mBook.get(position);
            ViewHolder viewHolder = new ViewHolder();
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_book_list
                        , parent, false);
                viewHolder.mButton = convertView.findViewById(R.id.book_button);
                viewHolder.mName = convertView.findViewById(R.id.book_title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.mName.setText(book.getBookname());

            String path = Environment.getExternalStorageDirectory() + "/imooc"
                    + book.getBookname() + ".txt";
            final File file = new File(path);

            viewHolder.mButton.setText(file.exists() ? "点击打开" : "点击下载");

            final ViewHolder finalViewHolder = viewHolder;
            viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file.exists()) {
                        // TODO: 2018/6/22 打开书籍
                        finalViewHolder.mButton.setText("打开");
                    } else {
                        mClient.addHeader("Accept-Encoding", "identity");
                        mClient.get(book.getBookfile(),
                                new FileAsyncHttpResponseHandler(file) {
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                        finalViewHolder.mButton.setText("下载失败");
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, File file) {
                                        finalViewHolder.mButton.setText("打开");
                                    }

                                    @Override
                                    public void onProgress(long bytesWritten, long totalSize) {
                                        super.onProgress(bytesWritten, totalSize);
                                        finalViewHolder
                                                .mButton
                                                .setText(String.valueOf(bytesWritten * 100 / totalSize) + "%");
                                    }
                                });
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            public TextView mName;
            public Button mButton;
        }
    }
}
