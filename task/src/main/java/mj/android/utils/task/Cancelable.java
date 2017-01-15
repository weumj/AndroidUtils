package mj.android.utils.task;

public interface Cancelable {
    /***
     * @return 취소 요청이 성공적인지 여부
     */
    boolean cancel();
}
