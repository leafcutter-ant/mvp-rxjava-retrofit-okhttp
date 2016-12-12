package com.winfo.wenjie.request;

import android.app.Dialog;
import android.text.TextUtils;
import java.net.SocketTimeoutException;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * @ProjectName: MvpRxjavaRetrofitDemo
 * @PackageName com.winfo.wenjie.request
 * @FileName: com.winfo.wenjie.request.DialogSubscriber.java
 * @Author: wenjie
 * @Date: 2016-12-12 14:23
 * @Description: 订阅者
 * @Version:
 */
public abstract class DialogSubscriber<T> extends Subscriber<T> implements  DialogCancelListener{

    /**
     * 定义一个请求成功的抽象方法 子类必须实现并在实现中进行处理服务器返回的数据
     * @param t 服务器返回的数据
     */
    protected abstract void onSuccess(T t);

    /**
     * 定义一个请求失败的抽象方法 子类必须实现并在实现中进行服务器返回数据的处理
     * @param msg 服务器返回的错误信息
     */
    protected abstract void onFailure(String msg);

    private DialogHandler dialogHandler;

    public DialogSubscriber(Dialog dialog){
        dialogHandler = new DialogHandler(dialog , this);
    }

    /**
     * 显示对话框 发送一个显示对话框的消息给dialoghandler  由他自己处理（也就是dialog中hanldermesage处理该消息）
     */
    private void showProgressDialog(){
        if (dialogHandler != null) {
            dialogHandler.obtainMessage(DialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
        }
    }

    /**
     * 隐藏对话框 ....
     */
    private void dismissProgressDialog(){
        if (dialogHandler != null) {
            dialogHandler.obtainMessage(DialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
            dialogHandler = null;
        }
    }

    /**
     * 请求开始
     */
    @Override
    public void onStart() {
        showProgressDialog();
    }

    /**
     * 请求完成
     */
    @Override
    public void onCompleted() {
        dismissProgressDialog();
    }


    /**
     * 请求出错
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        dismissProgressDialog();
        String msg = "";
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            int code = httpException.code();
            msg = httpException.getMessage();
            if (code == 504) {
                msg = "网络不给力";
            }
            if (code == 502 || code == 404) {
                msg = "服务器异常，请稍后再试";
            }
        } else if(e instanceof SocketTimeoutException){
            msg = "链接超时，请稍后重试！";
        }
        if (!TextUtils.isEmpty(msg)){
            onFailure(msg);
        }
    }


    /**
     * 请求成功
     * @param t
     */
    @Override
    public void onNext(T t) {
        /**
         * 请求成功将数据发出去
         */
        onSuccess(t);
    }


    /**
     * 请求被取消
     */
    @Override
    public void onCancel() {
        /**
         * 如果订阅者和被订阅者 没有取消订阅 则取消订阅 以取消网络请求
         */
        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }
    }
}
