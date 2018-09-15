package com.arcblock.batctool.adapter;

import android.support.annotation.Nullable;

import com.arcblock.batctool.R;
import com.arcblock.batctool.TransactionsByAddressForSenderQuery;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class TxsAsSenderRoleAdapter extends BaseQuickAdapter<TransactionsByAddressForSenderQuery.Datum, BaseViewHolder> {
	public TxsAsSenderRoleAdapter(int layoutResId, @Nullable List<TransactionsByAddressForSenderQuery.Datum> data) {
		super(layoutResId, data);
	}

	@Override
	protected void convert(BaseViewHolder helper, TransactionsByAddressForSenderQuery.Datum item) {
		helper.setText(R.id.txs_hash_tv, item.getHash() + "");
		helper.setText(R.id.txs_input_num_tv, item.getNumberInputs() + "");
		helper.setText(R.id.txs_output_num_tv, item.getNumberOutputs() + "");
		helper.setText(R.id.txs_block_height_tv, item.getBlockHeight() + "");
	}
}
