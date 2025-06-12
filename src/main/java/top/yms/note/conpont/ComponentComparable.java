package top.yms.note.conpont;

public interface ComponentComparable  extends Comparable<ComponentComparable> {

    /**
     * 组件排序，通过Collections.sort
     * <p>也就是sortValue越小，通过Collections.sort后就会越靠前</p>
     * @param other
     * @return
     */
    default int compareTo(ComponentComparable other) {
        return this.getSortValue() - other.getSortValue();
    }

    /**
     * <p>默认最小9999</p>
     * <p>子类若是在需要靠前时，清覆盖此方法并将值改小便可</p>
     * @return
     */
    default int getSortValue() {
        return 9999;
    }
}
