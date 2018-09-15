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
import com.arcblock.batctool.adapter.TxsAsSenderRoleAdapter;
import com.arcblock.batctool.type.PageInput;
import com.arcblock.corekit.ABCoreKitClient;
import com.arcblock.corekit.CoreKitPagedQuery;
import com.arcblock.corekit.bean.CoreKitBean;
import com.arcblock.corekit.bean.CoreKitPagedBean;
import com.arcblock.corekit.utils.CoreKitDiffUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class TxsAsSenderRoleActivity extends AppCompatActivity {

	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView mRecyclerView;
	private TxsAsSenderRoleAdapter mTxsAsSenderRoleAdapter;
	private List<TransactionsByAddressForSenderQuery.Datum> mDatumList = new ArrayList<>();
	private String address;
	private TransactionsByAddressForSenderQueryHelper mTransactionsByAddressForSenderQueryHelper;

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
				mTxsAsSenderRoleAdapter.notifyDataSetChanged();

				mTxsAsSenderRoleAdapter.setEnableLoadMore(false);
				mTransactionsByAddressForSenderQueryHelper.refresh();
			}
		});

		mRecyclerView = findViewById(R.id.rcv);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		mTxsAsSenderRoleAdapter = new TxsAsSenderRoleAdapter(R.layout.item_txs, mDatumList);
		mTxsAsSenderRoleAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
			@Override
			public void onLoadMoreRequested() {
				mTransactionsByAddressForSenderQueryHelper.loadMore();
			}
		}, mRecyclerView);
		mRecyclerView.setAdapter(mTxsAsSenderRoleAdapter);


		mTransactionsByAddressForSenderQueryHelper = new TransactionsByAddressForSenderQueryHelper(this, this, BATCToolApp.getInstance().abCoreKitClient());
		mTransactionsByAddressForSenderQueryHelper.setObserve(new Observer<CoreKitPagedBean<List<TransactionsByAddressForSenderQuery.Datum>>>() {
			@Override
			public void onChanged(@Nullable CoreKitPagedBean<List<TransactionsByAddressForSenderQuery.Datum>> coreKitPagedBean) {
				//1. handle return data
				if (coreKitPagedBean.getStatus() == CoreKitBean.SUCCESS_CODE) {
					if (coreKitPagedBean.getData() != null) {
						// new a old list
						List<TransactionsByAddressForSenderQuery.Datum> oldList = new ArrayList<>();
						oldList.addAll(mDatumList);

						// set mBlocks with new data
						mDatumList = coreKitPagedBean.getData();
						DiffUtil.DiffResult result = DiffUtil.calculateDiff(new CoreKitDiffUtil<>(oldList, mDatumList), true);
						// need this line , otherwise the update will have no effect
						mTxsAsSenderRoleAdapter.setNewData(mDatumList);
						result.dispatchUpdatesTo(mTxsAsSenderRoleAdapter);
					}
				}

				//2. view status change and loadMore component need
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setRefreshing(false);
				if (mTransactionsByAddressForSenderQueryHelper.isHasMore()) {
					mTxsAsSenderRoleAdapter.setEnableLoadMore(true);
					mTxsAsSenderRoleAdapter.loadMoreComplete();
				} else {
					mTxsAsSenderRoleAdapter.loadMoreEnd();
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
	 * TransactionsByAddressForSenderQueryHelper for TransactionsByAddressForSenderQuery
	 */
	private class TransactionsByAddressForSenderQueryHelper extends CoreKitPagedQuery<TransactionsByAddressForSenderQuery.Data, TransactionsByAddressForSenderQuery.Datum> {

		public TransactionsByAddressForSenderQueryHelper(FragmentActivity activity, LifecycleOwner lifecycleOwner, ABCoreKitClient client) {
			super(activity, lifecycleOwner, client);
		}

		@Override
		public List<TransactionsByAddressForSenderQuery.Datum> map(Response<TransactionsByAddressForSenderQuery.Data> dataResponse) {
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
			return TransactionsByAddressForSenderQuery.builder().sender(address).build();
		}

		@Override
		public Query getLoadMoreQuery() {
			PageInput pageInput = null;
			if (!TextUtils.isEmpty(getCursor())) {
				pageInput = PageInput.builder().cursor(getCursor()).build();
			}
			return TransactionsByAddressForSenderQuery.builder().sender(address).paging(pageInput).build();
		}

		@Override
		public Query getRefreshQuery() {
			return TransactionsByAddressForSenderQuery.builder().sender(address).build();
		}
	}
}
