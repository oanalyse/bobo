/**
 * 
 */
package com.browseengine.bobo.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.util.ReaderUtil;

import com.browseengine.bobo.facets.FacetHandler;

/**
 * @author ymatsuda
 *
 */
public class BoboBrowser extends MultiBoboBrowser
{
  private static Logger logger = Logger.getLogger(BoboBrowser.class);
  /**
   * @param reader BoboIndexReader (reader or subreader)
   * @throws IOException
   */
  public BoboBrowser(BoboIndexReader reader) throws IOException
  {
    super(createBrowsables(reader));
  }

  /**
   * @param reader BoboIndexReader (reader or subreader)
   * @throws IOException
   */
  public BoboBrowser(BoboIndexReader reader, Comparator<BoboIndexReader> readerComparator, Pruner pruner) throws IOException
  {
    super(createBrowsables(reader, readerComparator, pruner));
  }
  
  /**
   * @param reader List of BoboIndexReaders (reader or subreader)
   * @throws IOException
   */
  public BoboBrowser(List<BoboIndexReader> readerList, Comparator<BoboIndexReader> readerComparator, Pruner pruner) throws IOException
  {
    super(createBrowsables(readerList, readerComparator, pruner));
  }
  
  
  /**
   * @param reader BoboIndexReader (reader or subreader)
   * @return an array of Browsables each of which corresponds to a particular subReader
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader)
  {
    return createBrowsables(getSubReaderList(reader, null));
  }
  
  
  /**
   * @param readerList List of BoboIndexReaders (readre or subReader)
   * @return an array of Browsables each of which corresponds to a particular subReader
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList){
    return createBrowsables(readerList, null, null);
  }
  
  /**
   * 
   * @param reader BoboIndexReader (reader or subreader)
   * @param readerComparator Comparator to compare subReaders
   * @param pruner Pruner
   * @return an array of Browsables each of which corresponds to a particular subReader; the underlying pruned subReaders are in sorted order 
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader, Comparator<BoboIndexReader> readerComparator, Pruner pruner)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(reader, pruner), readerComparator);
  }
  
  /**
   * 
   * @param readerList List of BoboIndexReaders (reader or subreader)
   * @param readerComparator Comparator to compare subReaders
   * @param pruner Pruner
   * @return an array of Browsables each of which corresponds to a particular subReader; the underlying pruned subReaders are in sorted order 
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList,  Comparator<BoboIndexReader> readerComparator, Pruner pruner)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(readerList, pruner), readerComparator);
  }
  
  /**
   * 
   * @param reader BoboIndexReader (reader or subreader)
   * @param pruner Pruner
   * @return A list of pruned subReaders (none of them have further subReaders) 
   */
  private static List<BoboIndexReader> getSubReaderList(BoboIndexReader reader, Pruner pruner)
  {
    List<BoboIndexReader> readerList = new ArrayList<BoboIndexReader>();
    readerList.add(reader);
    return getSubReaderList(readerList, pruner);
  }
  
  /**
   * 
   * @param readerList List of BoboIndexReaders (reader or subreader)
   * @param pruner Pruner
   * @return A list of pruned subReaders (none of them have further subReaders) 
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
   * Build browsables on sorted readers and pick certain numbers of the sorted browsables
   * @param subReaderList List of BoboIndexReaders (none of them have further subReaders)
   * @param readerComparator Comparator to compare readers
   * @return An array of Browsables each of which corresponds to a particular subReader; the underlying pruned subReaders are in sorted order 
   */
  public static Browsable[] createBrowsablesFromSubReaderList(List<BoboIndexReader> subReaderList, Comparator<BoboIndexReader> readerComparator)
  {
    BoboIndexReader[] subReaders = subReaderList.toArray(new BoboIndexReader[subReaderList.size()]);
    if (readerComparator != null) Arrays.sort(subReaders, readerComparator);
    
    int size = subReaderList.size();
    Browsable[] subBrowsables = new Browsable[size];
    for(int i = 0; i < size; i++)
    {
      subBrowsables[i] = new BoboSubBrowser(subReaders[i]);
    }
    return subBrowsables;
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

