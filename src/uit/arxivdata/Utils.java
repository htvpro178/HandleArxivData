package uit.arxivdata;

//import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import javax.swing.plaf.ListUI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utils {
	private static float INFLUENCE = 0.1f;
	private List<String> listAuthors = new ArrayList<String>();
	private static double[] LISTINFLUENCE = { 0.1, 0.19, 0.271, 0.3439,
			0.40951, 0.468559, 0.5217031, 0.56953279, 0.612579511, 0.65132156,
			0.686189404, 0.717570464, 0.745813417, 0.771232075, 0.794108868,
			0.814697981, 0.833228183, 0.849905365, 0.864914828, 0.878423345 };

	public List<List<String>> ReadArXivData(String filePath) {
		List<List<String>> resultData = new ArrayList<List<String>>();

		try {

			// File fXmlFile = new File(filePath);
			// DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			// .newInstance();
			// DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			// Document doc = dBuilder.parse(fXmlFile);
			// doc.getDocumentElement().normalize();

			InputStream inputStream = new FileInputStream(filePath);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);

			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("authors");

			System.out.println("----------------------------");
			System.out.println("len=" + nList.getLength());
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					NodeList nListItem = eElement
							.getElementsByTagName("author");
					List<String> listKeyName = new ArrayList<String>();
					for (int j = 0; j < nListItem.getLength(); j++) {
						String keyName = eElement
								.getElementsByTagName("keyname").item(j)
								.getTextContent();
						System.out.println("keyname : " + keyName);
						listKeyName.add(keyName);
						if (!listAuthors.contains(keyName)) {
							listAuthors.add(keyName);
						}
					}
					resultData.add(listKeyName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultData;
	}

	public void identidyEgdesAndVertices(String xmlPath, String jsonPath) {
		List<List<String>> listData = new ArrayList<List<String>>();
		listData = ReadArXivData(xmlPath);
		System.out.println(listAuthors);
		Map<String, String> listAuth = new HashMap<String, String>(); // save
																		// vertexes
		for (int i = 1; i <= listAuthors.size(); i++) {
			listAuth.put(listAuthors.get(i - 1), String.valueOf(i));
		}
		// for (int i = 0; i <= listAuthors.size(); i++ ) {
		// System.out.print(listAuth[i] + ", ");
		// }
		System.out.println(listAuth);
		WriteJsonFile(listAuth, listData, jsonPath);

	}

	private void WriteJsonFile(Map<String, String> listAuth,
			List<List<String>> listData, String jsonPath) {
		JSONObject objectAll = new JSONObject();
		// vertices
		JSONArray vertices = new JSONArray();
		JSONObject name = new JSONObject();
		System.out.println("listAuth.size()=" + listAuth.size());
		for (int i = 1; i <= listAuth.size(); i++) {
			name = new JSONObject();
			name.put("Name", String.valueOf(i));
			vertices.add(name);
		}
		objectAll.put("vertices", vertices);
		List<int[]> listVertexes = GetVertexNameByKey(listData, listAuth);
		// Edges
		JSONArray arrayEgdes = new JSONArray();
		// Get list of Edges
		List<JSONObject> listResult = new ArrayList<JSONObject>();
		List<int[]> listAllEdges = new ArrayList<int[]>();
		for (int[] strEdges : listVertexes) {
			List<int[]> combine = findCombinations(strEdges); // combine with
																// set of
																// 2 vertexes
			listAllEdges.addAll(combine);
		}
		List<int[]> listEdgesRemoveDuplicate = new ArrayList<int[]>();

		for (int[] strEdges : listAllEdges) {
			// System.out.println("Key : " + entry.getKey() + " Value : "+
			// entry.getValue());
			List<JSONObject> listJsonEdges = new ArrayList<JSONObject>();
			if (countInfluenceOf2Vertex(strEdges, listEdgesRemoveDuplicate) == 0) {
				listJsonEdges = getJsonObjectFromList(strEdges,
						countInfluenceOf2Vertex(strEdges, listAllEdges));
				listEdgesRemoveDuplicate.add(strEdges);
			}
			for (JSONObject edgeInfo : listJsonEdges) {
				arrayEgdes.add(edgeInfo);
			}
		}

		objectAll.put("edges", arrayEgdes);

		try (FileWriter file = new FileWriter(jsonPath)) {
			file.write(objectAll.toJSONString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + objectAll);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<JSONObject> getJsonObjectFromList(int[] strEdges,
			int countAppear) {
		List<JSONObject> listResult = new ArrayList<JSONObject>();
		double directInfluence = 0.1f;
		if (countAppear <= 20) {
			directInfluence = LISTINFLUENCE[countAppear-1];
		} else {
			directInfluence = (double) (1 - Math.pow((double) 1 - INFLUENCE,
					(double) countAppear));
		}
		for (int aa = 0; aa < strEdges.length; aa++) {
			System.out.println(strEdges[aa]);
		}
		System.out.println(directInfluence);
		JSONObject edgeInfo = new JSONObject();
		edgeInfo.put("Start", String.valueOf(strEdges[0]));
		edgeInfo.put("End", String.valueOf(strEdges[1]));
		edgeInfo.put("DirectInfluence", directInfluence);
		listResult.add(edgeInfo);
		edgeInfo = new JSONObject();
		edgeInfo.put("Start", String.valueOf(strEdges[1]));
		edgeInfo.put("End", String.valueOf(strEdges[0]));
		edgeInfo.put("DirectInfluence", directInfluence);
		listResult.add(edgeInfo);

		return listResult;
	}

	private List<int[]> findCombinations(int[] input) {
		int k = 2;
		List<int[]> subsets = new ArrayList<>();
		int[] s = new int[k]; // here we'll keep indices
								// pointing to elements in input array

		if (k <= input.length) {
			// first index sequence: 0, 1, 2, ...
			for (int i = 0; (s[i] = i) < k - 1; i++)
				;
			subsets.add(getSubset(input, s));
			for (;;) {
				int i;
				// find position of item that can be incremented
				for (i = k - 1; i >= 0 && s[i] == input.length - k + i; i--)
					;
				if (i < 0) {
					break;
				} else {
					s[i]++; // increment this item
					for (++i; i < k; i++) { // fill up remaining items
						s[i] = s[i - 1] + 1;
					}
					subsets.add(getSubset(input, s));
				}
			}
		}
		return subsets;
	}

	private static int[] getSubset(int[] input, int[] subset) {
		int[] result = new int[subset.length];
		for (int i = 0; i < subset.length; i++)
			result[i] = input[subset[i]];
		return result;
	}

	private List<int[]> GetVertexNameByKey(List<List<String>> listData,
			Map<String, String> listAuth) {
		List<int[]> listVertexes = new ArrayList<int[]>();
		for (List<String> list : listData) {
			int[] arrayVertexes = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				arrayVertexes[i] = Integer.parseInt(listAuth.get(list.get(i)));
			}
			listVertexes.add(arrayVertexes);
		}
		return listVertexes;
	}

	private int countInfluenceOf2Vertex(int[] com, List<int[]> listVertexes) {
		int l_count = 0;
		for (int[] list : listVertexes) {
			if (checkValue2Array(com, list)) {
				l_count++;
			}
		}
		return l_count;
	}

	private boolean checkArrayBelong(int[] com, int[] list) {
		int count = 0;
		for (int jj = 0; jj < com.length; jj++) {
			for (int kk = 0; kk < list.length; kk++) {
				if (com[jj] == list[kk]) {
					count++;
				}
			}
		}
		if (count == 2) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkValue2Array(int[] array1, int[] array2) {
		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}
}
