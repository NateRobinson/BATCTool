package com.arcblock.batctool;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.arcblock.batctool.adapter.TxsAsReceiverRoleAdapter;
import com.arcblock.batctool.type.PageInput;
import com.arcblock.corekit.ABCoreKitClient;
import com.arcblock.corekit.CoreKitPagedQuery;
import com.arcblock.corekit.bean.CoreKitBean;
import com.arcblock.corekit.bean.CoreKitPagedBean;
import com.arcblock.corekit.utils.CoreKitDiffUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class TxsAsReceiverRoleActivity extends AppCompatActivity {

	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView mRecyclerView;
	private TxsAsReceiverRoleAdapter mTxsAsReceiverRoleAdapter;
	private List<TransactionsByAddressForReceiverQuery.Datum> mDatumList = new ArrayList<>();
	private String address;
	private TransactionsByAddressForReceiverQueryHelper mTransactionsByAddressForReceiverQueryHelper;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txs_as_sender_role);

		address = getIntent().getExtras().getString("address");

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("TxsAsSenderRole");

		mSwipeRefreshLayout = findViewById(R.id.swipe_view);

		mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mDatumList.clear();
				mTxsAsReceiverRoleAdapter.notifyDataSetChanged();

				mTxsAsReceiverRoleAdapter.setEnableLoadMore(false);
				mTransactionsByAddressForReceiverQueryHelper.refresh();
			}
		});

		mRecyclerView = findViewById(R.id.rcv);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		mTxsAsReceiverRoleAdapter = new TxsAsReceiverRoleAdapter(R.layout.item_txs, mDatumList);
		mTxsAsReceiverRoleAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
			@Override
			public void onLoadMoreRequested() {
				mTransactionsByAddressForReceiverQueryHelper.loadMore();
			}
		}, mRecyclerView);
		mRecyclerView.setAdapter(mTxsAsReceiverRoleAdapter);


		mTransactionsByAddressForReceiverQueryHelper = new TransactionsByAddressForReceiverQueryHelper(this, this, BATCToolApp.getInstance().abCoreKitClient());
		mTransactionsByAddressForReceiverQueryHelper.setObserve(new Observer<CoreKitPagedBean<List<TransactionsByAddressForReceiverQuery.Datum>>>() {
			@Override
			public void onChanged(@Nullable CoreKitPagedBean<List<TransactionsByAddressForReceiverQuery.Datum>> coreKitPagedBean) {
				//1. handle return data
				if (coreKitPagedBean.getStatus() == CoreKitBean.SUCCESS_CODE) {
					if (coreKitPagedBean.getData() != null) {
						// new a old list
						List<TransactionsByAddressForReceiverQuery.Datum> oldList = new ArrayList<>();
						oldList.addAll(mDatumList);

						// set mBlocks with new data
						mDatumList = coreKitPagedBean.getData();
						DiffUtil.DiffResult result = DiffUtil.calculateDiff(new CoreKitDiffUtil<>(oldList, mDatumList), true);
						// need this line , otherwise the update will have no effect
						mTxsAsReceiverRoleAdapter.setNewData(mDatumList);
						result.dispatchUpdatesTo(mTxsAsReceiverRoleAdapter);
					}
				}

				//2. view status change and loadMore component need
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setRefreshing(false);
				if (mTransactionsByAddressForReceiverQueryHelper.isHasMore()) {
					mTxsAsReceiverRoleAdapter.setEnableLoadMore(true);
					mTxsAsReceiverRoleAdapter.loadMoreComplete();
				} else {
					mTxsAsReceiverRoleAdapter.loadMoreEnd();
				}
			}
		});
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

	/**
	 * TransactionsByAddressForReceiverQueryHelper for TransactionsByAddressForReceiverQuery
	 */
	private class TransactionsByAddressForReceiverQueryHelper extends CoreKitPagedQuery<TransactionsByAddressForReceiverQuery.Data, TransactionsByAddressForReceiverQuery.Datum> {

		public TransactionsByAddressForReceiverQueryHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, ABCoreKitClient client) {
			super(activity, lifecycleOwner, client);
		}

		@Override
		public List<TransactionsByAddressForReceiverQuery.Datum> map(Response<TransactionsByAddressForReceiverQuery.Data> dataResponse) {
			if (dataResponse != null && dataResponse.data().getTransactionsByAddress() != null) {
				// set page info to CoreKitPagedQuery
				if (dataResponse.data().getTransactionsByAddress().getPage() != null) {
					// set is have next flag to CoreKitPagedQuery
					setHasMore(dataResponse.data().getTransactionsByAddress().getPage().isNext());
					// set new cursor to CoreKitPagedQuery
					setCursor(dataResponse.data().getTransactionsByAddress().getPage().getCursor());
				}
				return dataResponse.data().getTransactionsByAddress().getData();
			}
			return null;
		}

		@Override
		public Query getInitialQuery() {
			return TransactionsByAddressForReceiverQuery.builder().receiver(address).build();
		}

		@Override
		public Query getLoadMoreQuery() {
			PageInput pageInput = null;
			if (!TextUtils.isEmpty(getCursor())) {
				pageInput = PageInput.builder().cursor(getCursor()).build();
			}
			return TransactionsByAddressForReceiverQuery.builder().receiver(address).paging(pageInput).build();
		}

		@Override
		public Query getRefreshQuery() {
			return TransactionsByAddressForReceiverQuery.builder().receiver(address).build();
		}
	}
}
