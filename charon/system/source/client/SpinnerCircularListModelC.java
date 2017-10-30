// Charon system Mike Smith 1999-2017
package client;

import javax.swing.*;

public class SpinnerCircularListModelC extends SpinnerListModel
{
  private static final long serialVersionUID = 1L;

  public SpinnerCircularListModelC(Object[] items)
  {
    super(items);
  }

  // Returns the next value. If the current value is at the end
  // of the list, returns the first value.
  // There must be at least one item in the list.
  public Object getNextValue()
  {
    java.util.List list = getList();
    int index = list.indexOf(getValue());

    index = (index >= list.size()-1) ? 0 : index+1;
    return list.get(index);
  }

  // Returns the previous value. If the current value is at the
  // start of the list, returns the last value.
  // There must be at least one item in the list.
  public Object getPreviousValue()
  {
    java.util.List list = getList();
    int index = list.indexOf(getValue());

    index = (index <= 0) ? list.size()-1 : index-1;
    return list.get(index);
  }

}
