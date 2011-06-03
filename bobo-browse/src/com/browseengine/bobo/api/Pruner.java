package com.browseengine.bobo.api;

/**
 * @author hyan
 * @since 2.5.2
 */
public interface Pruner
{
  boolean isPruned(BoboIndexReader reader);
}
