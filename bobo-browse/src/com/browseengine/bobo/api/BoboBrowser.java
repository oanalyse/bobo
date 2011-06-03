/**
 * 
 */
package com.browseengine.bobo.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.util.ReaderUtil;

import com.browseengine.bobo.facets.FacetHandler;

/**
 * @author ymatsuda, hyan
 * 
 * The new APIs (2.5.2) introduce BoboIndexReaderComparator and Pruner to allows for more flexibility of BoboBrowser. 
 * BoboIndexReaderComparator is used in sorting different BoboIndexReaders.
 * Pruner is used in pruning BoboIndexReaders.
 * An application of BoboBrowser can plug in its own BoboIndexReaderComparator and Pruner to enable customized ordering and selecting of BoboSubBrowsers.
 * Note: "leaf subReader" can not have further child subReaders and "reader" may or may not have child subReaders. 
 */
public class BoboBrowser extends MultiBoboBrowser
{
  private static Logger logger = Logger.getLogger(BoboBrowser.class);

  /**
   * @param reader BoboIndexReader (either reader or leaf subReader)
   * @throws IOException
   * @since 2.5.0
   */
  public BoboBrowser(BoboIndexReader reader) throws IOException
  {
    super(createBrowsables(reader));
  }

  /**
   * @param reader BoboIndexReader (either reader or leaf subReader)
   * @param readerComparator Comparator to compare BoboIndexReaders (leaf subReaders)
   * @param pruner Pruner to prune BoboIndexReaders (leaf subReaders)
   * @throws IOException
   * @since 2.5.2
   */
  public BoboBrowser(BoboIndexReader reader, BoboIndexReaderComparator readerComparator, Pruner pruner) throws IOException
  {
    super(createBrowsables(reader, readerComparator, pruner));
  }

  /**
   * @param reader List of BoboIndexReaders (either readers or leaf subReaders)
   * @param readerComparator Comparator to compare BoboIndexReaders (leaf subReaders)
   * @param pruner Pruner to prune BoboIndexReaders (leaf subReaders)
   * @throws IOException
   * @since 2.5.2
   */
  public BoboBrowser(List<BoboIndexReader> readerList, BoboIndexReaderComparator readerComparator, Pruner pruner) throws IOException
  {
    super(createBrowsables(readerList, readerComparator, pruner));
  }


  /**
   * @param reader BoboIndexReader (either reader or leaf subReader)
   * @return An array of Browsables each of which corresponds to a particular BoboIndexReader (leaf subReader)
   * @since 2.5.0
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader)
  {
    return createBrowsables(getSubReaderList(reader, null));
  }


  /**
   * @param readerList List of BoboIndexReaders (either readers or leaf subReaders)
   * @return An array of Browsables each of which corresponds to a particular BoboIndexReader (leaf subReader)
   * @since 2.5.0
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList){
    return createBrowsables(readerList, null, null);
  }

  /**
   * Sort and prune all leaf subReaders of a given BoboIndexReader (either reader or leaf subReader) and build Browsables on the resulting leaf subReaders.   
   * The leaf subReaders will not be sorted if readerComparator is NULL. 
   * The leaf subReaders will not be pruned if pruner is NULL.
   * @param reader BoboIndexReader (either reader or leaf subReader)
   * @param readerComparator Comparator to compare BoboIndexReaders (leaf subReaders)
   * @param pruner Pruner to prune BoboIndexReaders (leaf subReaders)
   * @since 2.5.2
   * @return An array of Browsables each of which corresponds to a particular BoboIndexReader (leaf subReader)
   *            (those leaf subReaders are pruned by the pruner and sorted by the ordering specified in readerComparator) 
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader, BoboIndexReaderComparator readerComparator, Pruner pruner)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(reader, pruner), readerComparator);
  }

  /**
   * Sort and prune all leaf subReaders of a given list of BoboIndexReaders (either readers or leaf subReaders) and build Browsables on the resulting leaf subReaders.   
   * The leaf subReaders will not be sorted if readerComparator is NULL. 
   * The leaf subReaders will not be pruned if pruner is NULL.
   * @param readerList List of BoboIndexReaders (either readers or leaf subReaders)
   * @param readerComparator Comparator to compare BoboIndexReaders (leaf subReaders)
   * @param pruner Pruner to prune BoboIndexReaders (leaf subReaders)
   * @since 2.5.2
   * @return An array of Browsables each of which corresponds to a particular BoboIndexReader (leaf subReaders)
   *            (those leaf subReaders are pruned by the pruner and sorted by the ordering specified in readerComparator) 
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList,  BoboIndexReaderComparator readerComparator, Pruner pruner)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(readerList, pruner), readerComparator);
  }

  /**
   * Sort all leaf subReaders of a given list of BoboIndexReaders (either readers or leaf subReaders) and build Browsables on the resulting leaf subReaders.   
   * The leaf subReaders will not be sorted if readerComparator is NULL. 
   * @param subReaderList List of BoboIndexReaders (leaf subReaders)
   * @param readerComparator Comparator to compare BoboIndexReaders (leaf subReaders)
   * @since 2.5.2
   * @return An array of Browsables each of which corresponds to a particular BoboIndexReader (leaf subReader)
   *            (those leaf subReaders are sorted by the ordering specified in readerComparator)  
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList, BoboIndexReaderComparator readerComparator)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(readerList, null), readerComparator);
  }
  
  /**
   * Sort a given list of BoboIndexReaders (leaf subReaders) and build Browsables on the resulting leaf subReaders.   
   * The leaf subReaders will not be sorted if readerComparator is NULL. 
   * @param subReaderList List of BoboIndexReaders (leaf subReaders)
   * @param readerComparator Comparator to compare BoboIndexReaders (leaf subReaders)
   * @return An array of Browsables each of which corresponds to a particular BoboIndexReader (leaf subReader)
   *            (those leaf subReaders are sorted by the ordering specified in readerComparator)  
   */
  private static Browsable[] createBrowsablesFromSubReaderList(List<BoboIndexReader> subReaderList, BoboIndexReaderComparator readerComparator)
  {
    BoboIndexReader[] subReaders = subReaderList.toArray(new BoboIndexReader[subReaderList.size()]);
    int len = subReaders.length;
    if (len>1 && readerComparator != null) Arrays.sort(subReaders, readerComparator);

    Browsable[] subBrowsables = new Browsable[len];
    for(int i = 0; i < len; i++)
    {
      subBrowsables[i] = new BoboSubBrowser(subReaders[i]);
    }
    return subBrowsables;
  }
  
  /**
   * Get for a given reader all of its leaf subReaders excluding those pruned by the pruner.
   * The leaf subReaders will not be pruned if pruner is NULL.
   * @param reader BoboIndexReader (either reader or leaf subReaders)
   * @param pruner Pruner to prune BoboIndexReaders (leaf subReaders)
   * @return A list of pruned BoboIndexReaders (leaf subReaders) 
   */
  private static List<BoboIndexReader> getSubReaderList(BoboIndexReader reader, Pruner pruner)
  {
    List<BoboIndexReader> readerList = new ArrayList<BoboIndexReader>();
    readerList.add(reader);
    return getSubReaderList(readerList, pruner);
  }

  /**
   * Get for a given list of readers all of their leaf subReaders excluding those pruned by the pruner.
   * The leaf subReaders will not be pruned if pruner is NULL.
   * @param readerList List of BoboIndexReaders (reader or leaf subReaders)
   * @param pruner Pruner to prune BoboIndexReaders (leaf subReaders)
   * @return A list of pruned BoboIndexReaders (leaf subReaders) 
   */
  private static List<BoboIndexReader> getSubReaderList(List<BoboIndexReader> readerList, Pruner pruner)
  {
    List<BoboIndexReader> allSubReaderList = new ArrayList<BoboIndexReader>();
    for (BoboIndexReader reader : readerList)
    {
      List<BoboIndexReader> subReaderList = new ArrayList<BoboIndexReader>();
      ReaderUtil.gatherSubReaders(subReaderList, reader);
      if(subReaderList.size()<1)
      {
        allSubReaderList.add(reader);
      }
      else
      {
        allSubReaderList.addAll(subReaderList);
      }
    }

    if(pruner == null)
      return allSubReaderList;

    List<BoboIndexReader> prunedSubReaderList = null; 
    for(BoboIndexReader subReader : allSubReaderList)
    {
      boolean isPruned = pruner.isPruned(subReader);
      if(!isPruned)
      {
        if(prunedSubReaderList == null)
        {
          prunedSubReaderList = new ArrayList<BoboIndexReader>();
        }
        prunedSubReaderList.add(subReader);
      }
    }
    return prunedSubReaderList;
  }


  /**
   * Gets a set of facet names
   * 
   * @return set of facet names
   */
  public Set<String> getFacetNames()
  {
    return _subBrowsers[0].getFacetNames();
  }

  public FacetHandler<?> getFacetHandler(String name)
  {
    return _subBrowsers[0].getFacetHandler(name);
  }
}

