package com.browseengine.bobo.readersort;

import java.util.Comparator;

import com.browseengine.bobo.api.BoboIndexReader;

public interface BoboReaderComparator extends Comparator<BoboIndexReader>
{
  @Override
  public int compare(BoboIndexReader reader1, BoboIndexReader reader2);
}
