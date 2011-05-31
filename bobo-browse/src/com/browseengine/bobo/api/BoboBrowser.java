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
   * @param reader BoboIndexReader
   * @throws IOException
   */
  public BoboBrowser(BoboIndexReader reader) throws IOException
  {
    super(createBrowsables(reader));
  }

  /**
   * @param reader BoboIndexReader
   * @throws IOException
   */
  public BoboBrowser(BoboIndexReader reader, Comparator<BoboIndexReader> readerComparator, Set<BoboIndexReader> excludedSubReaders) throws IOException
  {
    super(createBrowsables(reader, readerComparator, excludedSubReaders));
  }
  
  /**
   * @param reader BoboIndexReader
   * @throws IOException
   */
  public BoboBrowser(BoboIndexReader reader, Comparator<BoboIndexReader> readerComparator, int firstSubReaderIndex, int numSubReaders) throws IOException
  {
    super(createBrowsables(reader, readerComparator, firstSubReaderIndex, numSubReaders));
  }
  
  /**
   * @param reader The reader could be a reader or subreader
   * @return an array of Browsables associated with the subReaders of the given input reader
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader)
  {
    return createBrowsables(getSubReaderList(reader, null));
  }
  
  
  /**
   * @param readerList Any reader of it could be a reader or subreader
   * @return an array of Browsables associated with the subReaders of the given input reader
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList){
    return createBrowsables(readerList, null, null);
  }
  
  /**
   * 
   * @param reader The reader could be a reader or subReader
   * @param readerComparator Comparator to compare subReaders
   * @param excludedSubReaders The set of excluded subReaders each of which cannot further have its subReaders
   * @return An array of Browsables associated with the sorted subReaders of the given input reader (excluding those subReaders in excludedSubReaders)
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader, Comparator<BoboIndexReader> readerComparator, Set<BoboIndexReader> excludedSubReaders)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(reader, excludedSubReaders), readerComparator);
  }
  
  /**
   * 
   * @param readerList The list of readers which can be readers or subReaders
   * @param readerComparator Comparator to compare subReaders
   * @param excludedSubReaders The set of excluded subReaders each of which cannot further have its subReaders
   * @return An array of Browsables associated with the sorted subReaders of the given input reader (excluding those subReaders in excludedSubReaders)
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList,  Comparator<BoboIndexReader> readerComparator, Set<BoboIndexReader> excludedSubReaders)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(readerList, excludedSubReaders), readerComparator);
  }
  
  /**
   * 
   * @param readerList The list of readers which can be readers or subReaders
   * @param readerComparator Comparator to compare subReaders
   * @param excludedSubReaders The set of excluded subReaders each of which cannot further have its subReaders
   * @return An array of Browsables associated with the sorted subReaders of the given input reader (excluding those subReaders in excludedSubReaders)
   */
  public static Browsable[] createBrowsables(List<BoboIndexReader> readerList,  Comparator<BoboIndexReader> readerComparator, int startSubReaderIndex, int numSubReaders)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(readerList, null), readerComparator, startSubReaderIndex, numSubReaders);
  }
  
  /**
   * 
   * @param reader The reader could be a reader or subReader
   * @param excludedSubReaders The set of excluded subReaders each of which cannot further have its subReaders
   * @return A list of subReaders (each of which does not have its subReaders) of the given reader excluding those subReaders in excludedSubReaders
   */
  private static List<BoboIndexReader> getSubReaderList(BoboIndexReader reader, Set<BoboIndexReader> excludedSubReaders)
  {
    List<BoboIndexReader> readerList = new ArrayList<BoboIndexReader>();
    readerList.add(reader);
    return getSubReaderList(readerList, excludedSubReaders);
  }
  
  /**
   * 
   * @param readerList The readerList cannot be null and at least contains one reader or subReader
   * @param excludedSubReaders The set of excluded subReaders each of which cannot further have its subReaders
   * @return A list of subReaders (each of which does not have its subReaders) of the given readerList excluding those subReaders in excludedSubReaders
   */
  private static List<BoboIndexReader> getSubReaderList(List<BoboIndexReader> readerList, Set<BoboIndexReader> excludedSubReaders)
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
    
    if(excludedSubReaders == null)
      return allSubReaderList;
    
    List<BoboIndexReader> prunedSubReaderList = null; 
    for(BoboIndexReader subReader : allSubReaderList)
    {
      boolean sss = excludedSubReaders.contains(subReader);
      if(!sss)
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
   * 
   * @param reader The reader could be a reader or subReader
   * @param readerComparator Comparator to compare subReaders
   * @param excludedSubReaders The set of excluded subReaders each of which cannot further have its subReaders
   * @return An array of Browsables associated with the sorted subReaders of the given input reader (excluding those subReaders in excludedSubReaders)
   */
  public static Browsable[] createBrowsables(BoboIndexReader reader, Comparator<BoboIndexReader> readerComparator, 
      int startSubReaderIndex, int numSubReaders)
  {
    return createBrowsablesFromSubReaderList(getSubReaderList(reader, null), readerComparator, startSubReaderIndex, numSubReaders);
  }
  
  /**
   * Build browsables on sorted readers and pick certain numbers of the sorted browsables
   * @param subReaderList A list of subReaders each of which cannot further have its subReaders
   * @param readerComparator Comparator to compare readers
   * @param startSubReaderIndex 
   * @param numSubReaders
   * @return An array of Browsables associated with the sorted subReaders of the given input subReaderList (excluding those subReaders in excludedSubReaders)
   */
  public static Browsable[] createBrowsablesFromSubReaderList(List<BoboIndexReader> subReaderList, Comparator<BoboIndexReader> readerComparator, 
                                                                                     int startSubReaderIndex, int numSubReaders)
  {
    BoboIndexReader[] subReaders = subReaderList.toArray(new BoboIndexReader[subReaderList.size()]);
    if (readerComparator != null) Arrays.sort(subReaders, readerComparator);
    
    numSubReaders = numSubReaders < subReaderList.size() ? numSubReaders : subReaderList.size();
    
    Browsable[] subBrowsables = new Browsable[numSubReaders];
    int size=0;
    for(int i = startSubReaderIndex; i < (startSubReaderIndex + numSubReaders) ; i++)
    {
      subBrowsables[size++] = new BoboSubBrowser(subReaders[i]);
    }
    return subBrowsables;
  }
  
  /**
   * 
   * @param subReaderList A list of subReaders each of which cannot further have its subReaders
   * @param readerComparator Comparator to compare readers
   * @return An array of Browsables associated with the sorted subReaders of the given input subReaderList (excluding those subReaders in excludedSubReaders)
   */
  public static Browsable[] createBrowsablesFromSubReaderList(List<BoboIndexReader> subReaderList, Comparator<BoboIndexReader> readerComparator)
  {
    return createBrowsablesFromSubReaderList(subReaderList, readerComparator,  0, subReaderList.size()-1);
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

