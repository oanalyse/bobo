package com.browseengine.bobo.pruner;

import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Pruner;
import com.browseengine.bobo.facets.data.FacetDataCache;

/**
 * @author hyan
 * @since 2.5.2
 * 
 * This pruner will indicate if a given BoboIndexReader should be pruned off.
 * In particular, the reader needs to be pruned if the latest time on its timeField is older than the given _timeBoundary.
 * 
 */
public class BoboIndexReaderTimeBasedPruner implements Pruner
{
  Long _timeBoundary;
  String _timeFacetName;

  /**
   * @param timeFacetName
   * @param timeBoundary
   */
  public BoboIndexReaderTimeBasedPruner(String timeFacetName, Long timeBoundary)
  {
    _timeBoundary = timeBoundary;
    _timeFacetName = timeFacetName;
  }

  @Override
  /**
   * @param reader BoboIndexReader
   * @return True if the latest time on the reader's timeField is older than the given _timeBoundary; otherwise return false.
   */
  public boolean isPruned(BoboIndexReader reader)
  {
    FacetDataCache<Long> data = (FacetDataCache<Long>) reader.getFacetData(_timeFacetName);
    if (data == null || data.valArray == null || data.valArray.size() <= 1)
      throw new IllegalStateException(_timeFacetName + " field does not exist in the reader or it is empty");

    Long time = Long.valueOf(data.valArray.get(data.valArray.size() - 1));
    if (time < _timeBoundary)  return true;
    return false;
  }
}
