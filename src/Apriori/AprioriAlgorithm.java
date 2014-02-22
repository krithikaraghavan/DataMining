package Apriori;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Krithika Raghavan 
 * - kr243 - 31255867
 *
 */
public class AprioriAlgorithm {
	HashMap<String, HashSet<String>> items;
	ArrayList<HashMap<HashSet<String>, Integer>> frequentItems;

	public HashMap<String, HashSet<String>> getItems() {
		return items;
	}

	// candidateItems ;
	File transactions = null;
	BufferedReader reader = null;
	Double minSupport = null;
	Double minConfidence = null;
	Integer noOfTransactions = null;

	AprioriAlgorithm() {
		items = new HashMap<String, HashSet<String>>();
		frequentItems = new ArrayList<HashMap<HashSet<String>, Integer>>();
		// candidateItems = new ArrayList<HashSet<String>>();
	}

	public Double getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(Double minSupport) {
		this.minSupport = minSupport;
	}

	public Double getMinConfidence() {
		return minConfidence;
	}

	public void setMinConfidence(Double minConfidence) {
		this.minConfidence = minConfidence;
	}

	public Integer getNoOfTransactions() {
		return noOfTransactions;
	}

	public void setNoOfTransactions(Integer noOfTransactions) {
		this.noOfTransactions = noOfTransactions;
	}

	/*
	 * Reads transactions and converts the transactions as inverted indexes of
	 * <Item, <List>transactions> key value pair;
	 */
	void readTransaction() {
		String newOrder = null;
		String[] itemsArray = null;
		Integer nOT = null;
		try {
			transactions = new File(".\\SportsAuthorityTransactions.txt");
			reader = new BufferedReader(new FileReader(transactions));
			nOT = new Integer(0);
			while ((newOrder = reader.readLine()) != null) {
				nOT += 1;
				itemsArray = newOrder.split(", ");
				for (int i = 1; i < itemsArray.length; i++) {
					HashSet<String> trans;
					if (!items.containsKey(itemsArray[i])) {
						trans = new HashSet<String>();
						trans.add(itemsArray[0]);
						items.put(itemsArray[i], trans);
					} else {
						trans = items.get(itemsArray[i]);
						trans.add(itemsArray[0]);
						items.put(itemsArray[i], trans);
					}
				}
			}
			this.setNoOfTransactions(nOT);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Counts the occurrence of the candidate item set in the transactions;
	 */
	public int getMinSupportCount(Set<String> candidateItems) {
		Set<String> toCompareWith = null;
		Object[] candItem = candidateItems.toArray();
		toCompareWith = items.get((candItem[0]));
		for (int i = 1; i < candItem.length; i++) {
			String nextItem = (String) candItem[i];
			Set<String> next = items.get(nextItem);
			if (next.size() < toCompareWith.size())
				toCompareWith = next;
		}
		Iterator<String> compare = toCompareWith.iterator();
		boolean contains = true;
		int count = 0;
		while (compare.hasNext()) {
			String toCompare = compare.next();
			contains = true;
			for (int i = 0; i < candItem.length; i++) {
				if (!(items.get(candItem[i]).contains(toCompare))) {
					contains = false;
					break;
				}
			}
			if (contains)
				count += 1;
		}
		return count;

	}

	/*
	 * Find frequent 1 itemsets and store in FrequentItemSets(0);
	 */

	public HashMap<HashSet<String>, Integer> findFrequent1ItemSets() {
		HashMap<HashSet<String>, Integer> frequent1ItemSets = new HashMap<HashSet<String>, Integer>();
		HashSet<String> itemSets = null;
		Iterator<String> keys = items.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			int count = items.get(key).size();
			if (count >= this.minSupport) {
				itemSets = new HashSet<String>();
				itemSets.add(key);
				frequent1ItemSets.put(itemSets, count);
			}
		}

		frequentItems.add(frequent1ItemSets);
		return frequent1ItemSets;
	}

	public ArrayList<HashMap<HashSet<String>, Integer>> getFrequentItems() {
		return frequentItems;
	}

	/*
	 * This procedure generates all candidate(k) sets and then uses Apriori
	 * property to elimate candidate sets that are infrequent
	 */
	@SuppressWarnings("unchecked")
	HashSet<HashSet<String>> aprioriGen(Set<HashSet<String>> fItem,
			int candidateNo) {
		Object[] fItems = fItem.toArray();
		// HashSet<String> candidates = new HashSet<String>();
		HashSet<HashSet<String>> candidates = new HashSet<HashSet<String>>();
		HashSet<String> cand = null;
		for (int i = 0; i < fItem.size(); i++) {
			HashSet<String> thisItem = (HashSet<String>) fItems[i];
			for (int k = i + 1; k < fItem.size(); k++) {
				cand = new HashSet<String>();
				cand.addAll(thisItem);
				cand.addAll((HashSet<String>) fItems[k]);
				if (cand.size() == (candidateNo + 1))
					candidates.add(cand);
			}
		}
		Iterator<HashSet<String>> iter = candidates.iterator();
		while (iter.hasNext()) {
			// for(HashSet<String> nextItem : candidates){
			HashSet<String> nextItem = iter.next();
			if (hasInfrequentItems(nextItem, fItem, candidateNo)) {
				iter.remove();
				// System.out.println ("Infrequent Item: "+ nextItem);
			}
		}

		// System.out.println(candidates);
		return candidates;
	}

	/*
	 * Once all the candidate(k) sets are generated, database is scanned For
	 * each candidate, generate the subset of candidates and and the count of
	 * the candidates are accumulated and returned.
	 */

	private boolean hasInfrequentItems(HashSet<String> thisCand,
			Set<HashSet<String>> fItem, int k) {

		List<Set<String>> subsets = getSubsets(thisCand, k);
		for (Set<String> c : subsets) {
			if (!fItem.contains(c))
				return true;
		}
		return false;
	}

	/*
	 * Generates all subsets of a given set
	 */

	private static void getSubsets(HashSet<String> superSet, Integer k,
			int idx, Set<String> current, List<Set<String>> res) {
		Object[] sSet = superSet.toArray();
		// successful stop clause
		if (current.size() == k) {
			res.add(new HashSet<String>(current));
			return;
		}

		// unseccessful stop clause
		if (idx == sSet.length)
			return;
		String x = (String) sSet[idx];
		current.add(x);
		// "guess" x is in the subset
		getSubsets(superSet, k, idx + 1, current, res);
		current.remove(x);
		// "guess" x is not in the subset
		getSubsets(superSet, k, idx + 1, current, res);

	}

	public static List<Set<String>> getSubsets(HashSet<String> superSet, int k) {
		List<Set<String>> res = new ArrayList<>();
		getSubsets(superSet, k, 0, new HashSet<String>(), res);
		return res;
	}

}
