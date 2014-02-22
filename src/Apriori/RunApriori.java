package Apriori;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Krithika Raghavan 
 * - kr243 - 31255867
 *
 */
public class RunApriori {
	public static void main(String args[]) {
		AprioriAlgorithm apriori = new AprioriAlgorithm();
		
		/* 
		 * Creating an inverted index of items, transactions list
		 * Reads the transaction file and converts the transactions in the file to 
		 * <Items,List<Transaction>> key value pairs and stores it in the HashMap items
		 * for easier reference 
		 */
		apriori.readTransaction();
		//Printing all <Items,List<Transaction>> key value pairs 
		/*HashMap<String, HashSet<String>> itemsA = apriori.getItems();
		for(Entry<String, HashSet<String>> e: itemsA.entrySet()){
			System.out.println(e.getKey()+"= "+e.getValue());	
		}*/
		
		/*
		 * Get Min. Support and Min. Confidence values from the user
		 * Not expressed as percentage
		 * Input accepted as floating point number between .01 and 1
		 */
		Scanner readInput = new Scanner(System.in);
		boolean exception = false;
		do{
			exception = false;
		try{
			System.out.println("Enter the min. support value:");
			apriori.setMinSupport(readInput.nextDouble()
				* apriori.getNoOfTransactions());
			System.out.println("Enter the min. confidence value:");
			apriori.setMinConfidence(readInput.nextDouble());
		}catch(InputMismatchException e){
			System.out.println("Please input floating point number for " +
					"Min Support and Min Confidence values");
			readInput.next();
			exception = true;
		}finally{
			readInput.close();
		}
		}while(exception);
		
		/*
		 * Find frequent 1 item sets, frequentItems;
		 */
		apriori.findFrequent1ItemSets();
		
		/*
		 * frequentItems(k-1) is used to generate Candidate candidate(k) to find
		 * frequentItems(k) for k>=2 
		 * (In this case k>=1 because frequent items index starts from 0)
		 * 
		 */
		for (int k = 1; !apriori.frequentItems.get(k - 1).isEmpty(); k++) {
			HashMap<HashSet<String>, Integer> fItem = apriori.frequentItems
					.get(k - 1);
			HashMap<HashSet<String>, Integer> freqSets = new HashMap<HashSet<String>, Integer>();
			HashSet<HashSet<String>> candidate = null;
			candidate = apriori.aprioriGen(fItem.keySet(), k);
			int count = 0;
			/*
			 * All candidates satifying the Min Support form 
			 * the frequent k itemsets 
			 */
			for (HashSet<String> c : candidate) {
				if ((count = apriori.getMinSupportCount(c)) >= apriori
						.getMinSupport())
					freqSets.put(c, count);
			}
			apriori.frequentItems.add(k, freqSets);
		}
		/*
		 * Once frequent ItemSets has been found, association rules are generated:
		 * 1. For each frequent itemset f, generate all non-empty subsets of f;
		 * 2. For each non-empty subset s of f, input s=> (f-s) 
		 *    if(support_count(f)/support_count(s))> Min Confidence
		 */
		ArrayList<HashMap<HashSet<String>, Integer>> freqItems = apriori
				.getFrequentItems();
		int i = 1;
		HashSet<String> remaining = null;
		
		// 1. For each frequent itemset f, generate all non-empty subsets of f;
		 
		for (HashMap<HashSet<String>, Integer> f : freqItems) {
			for (Entry<HashSet<String>, Integer> e : f.entrySet()) {
				HashSet<String> freqItem = e.getKey();
				Integer freqItemCount = e.getValue();
				List<Set<String>> subsets = new ArrayList<Set<String>>();
				for (int k = 1; k < freqItem.size(); k++) {
					subsets.addAll(AprioriAlgorithm.getSubsets(freqItem, k));
				}
		/*
		 * 2. For each non-empty subset s of f, input s=> (f-s) 
		 * if(support_count(f)/support_count(s))> Min Confidence
		 */	
				for (Set<String> s : subsets) {
					if (s.size() != 0) {
						Integer supportCountOfS = getSupportCountOf(s,
								freqItems);
						Double confidence = (double) freqItemCount
								/ (double) supportCountOfS;
						if (confidence >= (apriori.getMinConfidence())) {
							remaining = new HashSet<String>(freqItem);
							remaining.removeAll(s);
							System.out.println(s + " => " + remaining);
						}
					}
				}
			}
			i++;
		}
	}

	private static Integer getSupportCountOf(Set<String> s,
			ArrayList<HashMap<HashSet<String>, Integer>> freqItems) {
		return freqItems.get((s.size() - 1)).get(s);
	}
}
