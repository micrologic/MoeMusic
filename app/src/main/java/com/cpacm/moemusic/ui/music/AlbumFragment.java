package com.cpacm.moemusic.ui.music;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.cpacm.core.bean.WikiBean;
import com.cpacm.core.http.RxBus;
import com.cpacm.core.mvp.views.AlbumIView;
import com.cpacm.moemusic.MoeApplication;
import com.cpacm.moemusic.R;
import com.cpacm.moemusic.event.FavEvent;
import com.cpacm.moemusic.ui.BaseFragment;
import com.cpacm.moemusic.ui.adapters.AlbumAdapter;
import com.cpacm.moemusic.ui.widgets.RefreshRecyclerView;

import net.cpacm.library.SimpleSliderLayout;
import net.cpacm.library.indicator.ViewpagerIndicator.CirclePageIndicator;
import net.cpacm.library.slider.BaseSliderView;
import net.cpacm.library.slider.ImageSliderView;
import net.cpacm.library.slider.OnSliderClickListener;

import java.util.List;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * @Auther: cpacm
 * @Date: 2016/7/9.
 * @description: 音乐界面
 */
public class AlbumFragment extends BaseFragment implements RefreshRecyclerView.RefreshListener, AlbumIView {
    public static final String TITLE = MoeApplication.getInstance().getString(R.string.album);

    private RefreshRecyclerView refreshView;
    private View headerView;
    private AlbumAdapter albumAdapter;
    private SimpleSliderLayout sliderLayout;
    private CirclePageIndicator circlePageIndicator;
    private GridLayoutManager gridLayoutManager;

    private CompositeSubscription allSubscription = new CompositeSubscription();
    private AlbumPresenter albumPresenter;

    private String[] strs = {"夜空", "车站", "夕阳", "世界", "神社", "碑"};
    private String[] urls = {
            "http://7xi4up.com1.z0.glb.clouddn.com/%E5%A3%81%E7%BA%B81.jpg",
            "http://7xi4up.com1.z0.glb.clouddn.com/%E5%A3%81%E7%BA%B82.jpg",
            "http://7xi4up.com1.z0.glb.clouddn.com/%E5%A3%81%E7%BA%B83.jpg",
            "http://7xi4up.com1.z0.glb.clouddn.com/%E5%A3%81%E7%BA%B84.jpg",
            "http://7xi4up.com1.z0.glb.clouddn.com/%E5%A3%81%E7%BA%B85.jpg",
            "http://7xi4up.com1.z0.glb.clouddn.com/%E5%A3%81%E7%BA%B86.jpg"
    };

    public static AlbumFragment newInstance() {
        AlbumFragment fragment = new AlbumFragment();
        return fragment;
    }

    public AlbumFragment() {
        albumPresenter = new AlbumPresenter(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allSubscription.add(RxBus.getDefault()
                .toObservable(FavEvent.class).subscribe(new Action1<FavEvent>() {
                    @Override
                    public void call(FavEvent favEvent) {
                        onEvent(favEvent);
                    }
                }));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parentView = inflater.inflate(R.layout.fragment_album, container, false);

        refreshView = (RefreshRecyclerView) parentView.findViewById(R.id.refresh_view);
        headerView = inflater.inflate(R.layout.recycler_album_header, container, false);

        initRefreshView();

        initSlider();
        return parentView;
    }

    private void initRefreshView() {
        albumAdapter = new AlbumAdapter(getActivity());
        refreshView.setAdapter(albumAdapter);
        refreshView.setHeaderView(headerView);
        refreshView.setRefreshListener(this);
        refreshView.setLoadEnable(false);
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        refreshView.setLayoutManager(gridLayoutManager);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (refreshView.getRefreshRecycleAdapter().getItemViewType(position)) {
                    case RefreshRecyclerView.RefreshRecycleAdapter.HEADER:
                    case RefreshRecyclerView.RefreshRecycleAdapter.LOADMORE:
                    case AlbumAdapter.ALBUM_TYPE_NEW:
                    case AlbumAdapter.ALBUM_TYPE_HOT:
                        return gridLayoutManager.getSpanCount();
                    default:
                        return 1;
                }
            }
        });
        refreshView.startSwipeAfterViewCreate();
    }

    private void initSlider() {
        sliderLayout = (SimpleSliderLayout) headerView.findViewById(R.id.simple_slider);
        circlePageIndicator = (CirclePageIndicator) headerView.findViewById(R.id.circle_indicator);
        for (int i = 0; i < urls.length; i++) {
            ImageSliderView sliderView = new ImageSliderView(getActivity());
            sliderView.empty(R.drawable.image_empty);
            Glide.with(this).load(urls[i]).crossFade().into(sliderView.getImageView());
            sliderView.setPageTitle(strs[i]);
            sliderLayout.addSlider(sliderView);
            sliderView.setOnSliderClickListener(new OnSliderClickListener() {
                @Override
                public void onSliderClick(BaseSliderView slider) {
                    SongPlayerActivity.open(getActivity());
                }
            });
        }
        sliderLayout.setViewPagerIndicator(circlePageIndicator);//为viewpager设置指示器
        sliderLayout.setCycling(true);
    }

    @Override
    public void onSwipeRefresh() {
        albumPresenter.requestAlbumIndex();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void getMusics(List<WikiBean> newMusics, List<WikiBean> hotMusics) {
        albumAdapter.setNewMusics(newMusics);
        albumAdapter.setHotMusics(hotMusics);
        refreshView.notifyDataSetChanged();
        refreshView.notifySwipeFinish();
    }

    public void onEvent(FavEvent favEvent) {
        albumAdapter.updateWikiFav(favEvent.getWikiId(), favEvent.isFav());
    }

    @Override
    public void loadMusicFail(String msg) {
        refreshView.notifySwipeFinish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!allSubscription.isUnsubscribed()) {
            allSubscription.unsubscribe();
        }
    }
}
