package com.browseengine.bobo.readersort;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.facets.data.FacetDataCache;

/**
 * @author hyan
 *  Compare two BoboReaders by the time field. In order to use this Comparator, 
 *  Each document in the index has to have a non-empty time field whose value is long type. 
 *  Bobo has to have a simpleFacetHandler on it.
 */
public class BoboReaderTimeBasedComparator implements BoboReaderComparator
{
  private final String _timeFieldName;
  private final boolean _reverse;

  /**
   * @param timefieldname
   * @param reverse
   */
  public BoboReaderTimeBasedComparator(String timeFieldName, boolean reverse)
  {
    _timeFieldName = timeFieldName;
    _reverse = reverse;
  }

  @Override
  public int compare(BoboIndexReader reader1, BoboIndexReader reader2)
  {
    @SuppressWarnings("unchecked")
    FacetDataCache<Long> data1 = (FacetDataCache<Long>) reader1.getFacetData(_timeFieldName);
    if (data1 == null || data1.valArray==null || data1.valArray.size()<=1) throw new IllegalStateException(_timeFieldName + " field does not exist in the reader or it is empty");
    
    @SuppressWarnings("unchecked")
    FacetDataCache<Long> data2 = (FacetDataCache<Long>) reader2.getFacetData(_timeFieldName);
    if (data2 == null || data2.valArray==null || data2.valArray.size()<=1) throw new IllegalStateException(_timeFieldName + " field does not exist in the reader or it is empty");
    
    // the first value of valArray is a dummy value, so we only compare the second value of valArray (which is the minimum value of valArray) and the last value
    // of valArray which is the maximum value of valArray. 
    
    Long startTime1 = Long.valueOf(data1.valArray.get(1));
    Long endTime1 = Long.valueOf(data1.valArray.get(data1.valArray.size()-1));
    
    Long startTime2 = Long.valueOf(data2.valArray.get(1));
    Long endTime2 = Long.valueOf(data2.valArray.get(data2.valArray.size()-1));
    
    // the larger the time value is, the more recent the time is.
    int ret;
    if(endTime1 < startTime2) ret = -1;
    else if(startTime2 > endTime2) ret=1;
    else return ret=0;  // if there is overlap, we treat them equal
    
    if(_reverse) ret = (-1) * ret;
    return ret;
  }

}
