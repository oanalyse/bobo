package com.browseengine.bobo.api;

import java.util.Comparator;

/**
 * 
 * @author hyan
 * @since 2.5.2
 */
public interface BoboIndexReaderComparator extends Comparator<BoboIndexReader>
{
  @Override
  public int compare(BoboIndexReader reader1, BoboIndexReader reader2);
}