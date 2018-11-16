package com.arcblock.batctool;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apollographql.apollo.api.Query;
import com.arcblock.batctool.adapter.TxsAsReceiverRoleAdapter;
import com.arcblock.batctool.type.PageInput;
import com.arcblock.corekit.CoreKitPagedQuery;
import com.arcblock.corekit.CoreKitPagedQueryResultListener;
import com.arcblock.corekit.PagedQueryHelper;
import com.arcblock.corekit.utils.CoreKitDiffUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class TxsAsReceiverRoleActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private TxsAsReceiverRoleAdapter mTxsAsReceiverRoleAdapter;
    private List<TransactionsByAddressQuery.Datum> mDatumList = new ArrayList<>();
    private String address;
    private LineChart mLineChart;
    private boolean isShowChart = false;
    private Button mButton;

    private PagedQueryHelper<TransactionsByAddressQuery.Data, TransactionsByAddressQuery.Datum> mPagedQueryHelper;
    private CoreKitPagedQuery<TransactionsByAddressQuery.Data, TransactionsByAddressQuery.Datum> mCoreKitPagedQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txs_as_reveiver_role);

        address = getIntent().getExtras().getString("address");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("TxsAsSenderRole");

        mSwipeRefreshLayout = findViewById(R.id.swipe_view);
        mLineChart = findViewById(R.id.chart);
        mButton = findViewById(R.id.change_btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowChart = !isShowChart;
                refresh();
            }
        });

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDatumList.clear();
                mTxsAsReceiverRoleAdapter.notifyDataSetChanged();

                mTxsAsReceiverRoleAdapter.setEnableLoadMore(false);
                mCoreKitPagedQuery.startInitQuery();
            }
        });

        mRecyclerView = findViewById(R.id.rcv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mTxsAsReceiverRoleAdapter = new TxsAsReceiverRoleAdapter(R.layout.item_txs, mDatumList);

        // empty view
        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_of_txs, null);
        TextView address_tv = emptyView.findViewById(R.id.address_tv);
        address_tv.setText(address + " is right?");
        mTxsAsReceiverRoleAdapter.setEmptyView(emptyView);

        mTxsAsReceiverRoleAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mCoreKitPagedQuery.startLoadMoreQuery();
            }
        }, mRecyclerView);
        mRecyclerView.setAdapter(mTxsAsReceiverRoleAdapter);

        mPagedQueryHelper = new PagedQueryHelper<TransactionsByAddressQuery.Data, TransactionsByAddressQuery.Datum>() {
            @Override
            public Query getInitialQuery() {
                return TransactionsByAddressQuery.builder().receiver(address).build();
            }

            @Override
            public Query getLoadMoreQuery() {
                PageInput pageInput = null;
                if (!TextUtils.isEmpty(getCursor())) {
                    pageInput = PageInput.builder().cursor(getCursor()).build();
                }
                return TransactionsByAddressQuery.builder().receiver(address).paging(pageInput).build();
            }

            @Override
            public List<TransactionsByAddressQuery.Datum> map(TransactionsByAddressQuery.Data data) {
                if (data.getTransactionsByAddress() != null) {
                    // set page info to CoreKitPagedQuery
                    if (data.getTransactionsByAddress().getPage() != null) {
                        // set is have next flag to CoreKitPagedQuery
                        setHasMore(data.getTransactionsByAddress().getPage().isNext());
                        // set new cursor to CoreKitPagedQuery
                        setCursor(data.getTransactionsByAddress().getPage().getCursor());
                    }
                    return data.getTransactionsByAddress().getData();
                }
                return null;
            }
        };

        mCoreKitPagedQuery = new CoreKitPagedQuery<>(this, BATCToolApp.getInstance().abCoreKitClient(), mPagedQueryHelper);
        mCoreKitPagedQuery.setPagedQueryResultListener(new CoreKitPagedQueryResultListener<TransactionsByAddressQuery.Datum>() {
            @Override
            public void onSuccess(List<TransactionsByAddressQuery.Datum> list) {
                //1. handle return data
                // new a old list
                List<TransactionsByAddressQuery.Datum> oldList = new ArrayList<>();
                oldList.addAll(mDatumList);

                // set mBlocks with new data
                mDatumList = list;
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new CoreKitDiffUtil<>(oldList, mDatumList), true);
                // need this line , otherwise the update will have no effect
                mTxsAsReceiverRoleAdapter.setNewData(mDatumList);
                result.dispatchUpdatesTo(mTxsAsReceiverRoleAdapter);

                //2. view status change and loadMore component need
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
                if (mPagedQueryHelper.isHasMore()) {
                    mTxsAsReceiverRoleAdapter.setEnableLoadMore(true);
                    mTxsAsReceiverRoleAdapter.loadMoreComplete();
                } else {
                    mTxsAsReceiverRoleAdapter.loadMoreEnd();
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        mCoreKitPagedQuery.startInitQuery();


        refresh();
    }

    private void refresh() {
        if (isShowChart) {
            mRecyclerView.setVisibility(View.GONE);
            mLineChart.setVisibility(View.VISIBLE);
            mButton.setText("Show List View");


            List<Entry> entries = new ArrayList<>();

            int i = 1;

            for (TransactionsByAddressQuery.Datum data : mDatumList) {
                // turn your data into Entry objects
                entries.add(new Entry(i++, data.getNumberOutputs()));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
            //dataSet.setColor(...);
            //dataSet.setValueTextColor(...); // styling, .
            LineData lineData = new LineData(dataSet);
            mLineChart.setData(lineData);
            mLineChart.invalidate(); // refresh
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mLineChart.setVisibility(View.GONE);
            mButton.setText("Show Chart View");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
