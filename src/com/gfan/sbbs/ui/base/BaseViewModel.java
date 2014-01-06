/*
 * from aSM 
 */
package com.gfan.sbbs.ui.base;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseViewModel {
	protected OnTabIndexChangeListener mOnTabIndexChangeListener;
	
	public interface OnViewModelChangObserver {

		public void onViewModelChange(BaseViewModel viewModel,
				String changedPropertyName, Object... params);

	}

	public interface OnTabIndexChangeListener{
		public void onTabIndexChange(int tabIndex);
	}
	
	private List<OnViewModelChangObserver> m_changeObserverList 
				= new ArrayList<BaseViewModel.OnViewModelChangObserver>();

	public void setOnTabIndexChangeListener(OnTabIndexChangeListener listener){
		this.mOnTabIndexChangeListener = listener;
	};
	public void registerViewModelChangeObserver(
			OnViewModelChangObserver observer) {
		m_changeObserverList.add(observer);
	}

	public void unregisterViewModelChangeObserver(
			OnViewModelChangObserver observer) {
		m_changeObserverList.remove(observer);
	}

	public void notifyViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		for (Iterator<OnViewModelChangObserver> iterator = m_changeObserverList
				.iterator(); iterator.hasNext();) {
			OnViewModelChangObserver changeObserver = iterator.next();
			changeObserver.onViewModelChange(viewModel, changedPropertyName,
					params);

		}
	}

}
