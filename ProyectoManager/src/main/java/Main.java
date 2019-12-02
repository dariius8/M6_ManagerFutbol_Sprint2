import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.StoredProcedureQuery;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

	private static EntityManagerFactory emf;
	private static EntityManager em;
	private static StoredProcedureQuery storedProcedure;

	public static void main(String[] args) {
		tractamentXML();
	}

	public static void tractamentXML() {
		File fichero = new File("..\\ProyectoManager\\src\\main\\resources\\config.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fichero);
			doc.getDocumentElement().normalize();
			// ruta phpmyadmin
			Node nNode = doc.getElementsByTagName("ruta_phpmyadmin").item(0);
			String ruta_phpmyadmin = nNode.getTextContent();
			// ruta bd mysql
			nNode = doc.getElementsByTagName("ruta_bd_mysql").item(0);
			String rutaBdMysql = nNode.getTextContent();
			// ruta equips
			nNode = doc.getElementsByTagName("ruta_equips").item(0);
			String rutaEquips = nNode.getTextContent();
			// ruta jugadors
			nNode = doc.getElementsByTagName("ruta_jugadors").item(0);
			String rutaJugadors = nNode.getTextContent();
			// ruta txt jornada1
			nNode = doc.getElementsByTagName("ruta_jornada1").item(0);
			String rutaJornada1 = nNode.getTextContent();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("..\\ProyectoManager\\src\\main\\resources\\config.xml"));
			transformer.transform(source, result);
			conexion(ruta_phpmyadmin);
			menu(rutaBdMysql, rutaEquips, rutaJugadors, rutaJornada1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void conexion(String ruta_phpmyadmin) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(ruta_phpmyadmin, "root", "");
			PreparedStatement ps = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS manager_futbol");
			ps.executeUpdate();
			System.out.println("BD Manager Futbol creada (sino existeix).");
			System.out.println(
					"Importa el arxiu 'manager_futbol.sql' abans de escollir una opcio del menu (nomes el primer cop).");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	public static void menu(String rutaBdMysql, String rutaEquips, String rutaJugadors, String rutaJornada1) {
		Scanner lector = new Scanner(System.in);
		int i = 0;
		while (i != 6) {
			System.out.println("\nMENU");
			System.out.println("   1. Inicialitzar la base de dades");
			System.out.println("   2. Mostrar la classificacio");
			System.out.println("   3. Simular la jornada 1 i mostrar els resultats");
			System.out.println("   4. Mostrar els resultats de la jornada 1 ");
			System.out.println("   5. Actualitzar classificacio a la jornada 1");
			System.out.println("   6. Sortir");
			System.out.print("Escull una opcio: ");
			i = lector.nextInt();
			if (i > 0 && i < 7) {
				switch (i) {
				case 1:
					crearTaules();
					insertsEquips(rutaBdMysql, rutaEquips);
					insertsJugadors(rutaBdMysql, rutaJugadors);
					insertsClassificacio(rutaBdMysql);
					break;
				case 2:
					mostrarClassificacio(rutaBdMysql);
					break;
				case 3:
					llegirTxt(rutaJornada1, rutaBdMysql);
					break;
				case 4:
					mostrarPartits(rutaBdMysql);
					break;
				case 5:
					actualizarClassificacio(rutaBdMysql);
					break;
				default:
					System.out.println("\nAdeu!");
					break;
				}
			} else
				System.out.println("\nError! Valor incorrecte.");
		}
	}

	public static void crearTaules() {
		emf = Persistence.createEntityManagerFactory("ManagerFutbol");
		em = emf.createEntityManager();
		storedProcedure = em.createStoredProcedureQuery("crearTaules");
		try {
			em.getTransaction().begin();
			storedProcedure.execute();
			em.getTransaction().commit();
			System.out.println("Procedure 'crearTaules' carregat.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			em.close();
			emf.close();
		}
	}

	public static void insertsEquips(String rutaBdMysql, String rutaEquips) {
		File fichero = new File(rutaEquips);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fichero);
			doc.getDocumentElement().normalize();
			// comprobamos si hay datos
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Equips");
			ResultSet rs = pstmt.executeQuery();
			// si no hay los inserta
			if (rs.next() == false) {
				System.out.println();
				// Inserts Equips
				// nodo = equip y lo vamos recorriendo
				NodeList nList = doc.getElementsByTagName("equip");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					emf = Persistence.createEntityManagerFactory("ManagerFutbol");
					em = emf.createEntityManager();
					storedProcedure = em.createStoredProcedureQuery("insertarEquips");
					em.getTransaction().begin();
					storedProcedure.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(3, Integer.class, ParameterMode.IN);
					Node nNode = nList.item(temp);
					// obtenemos los atributos y su valor
					NamedNodeMap nodeMap = nNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node tempNode = nodeMap.item(i);
						// insert id_equip
						int id_equip = Integer.parseInt(tempNode.getNodeValue());
						storedProcedure.setParameter(1, id_equip);
						// nodo = nom_equip
						NodeList nList2 = doc.getElementsByTagName("nom_equip");
						Node nNode2 = nList2.item(temp);
						// insert nom_equip
						storedProcedure.setParameter(2, nNode2.getTextContent());
						// nodo = qualitat
						NodeList nList3 = doc.getElementsByTagName("qualitat");
						Node nNode3 = nList3.item(temp);
						// insert qualitat
						int qualitat = Integer.parseInt(nNode3.getTextContent());
						storedProcedure.setParameter(3, qualitat);
						storedProcedure.execute();
						em.getTransaction().commit();
						System.out.println("Equip '" + nNode2.getTextContent() + "' insertat en la taula Equips.");
					}
				}
			} else
				System.out.println("\nJa has insertat les dades de la taula Equips!");
			conn.close();
			pstmt.close();
			rs.close();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(rutaEquips));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertsJugadors(String rutaBdMysql, String rutaJugadors) {
		File fichero = new File(rutaJugadors);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fichero);
			doc.getDocumentElement().normalize();
			// comprobamos si hay datos
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Jugadors");
			ResultSet rs = pstmt.executeQuery();
			// si no hay los inserta
			if (rs.next() == false) {
				System.out.println();
				// Inserts Jugadors
				// nodo = jugador y lo vamos recorriendo
				NodeList nList = doc.getElementsByTagName("jugador");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					emf = Persistence.createEntityManagerFactory("ManagerFutbol");
					em = emf.createEntityManager();
					storedProcedure = em.createStoredProcedureQuery("insertarJugadors");
					em.getTransaction().begin();
					storedProcedure.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(4, Integer.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(5, String.class, ParameterMode.IN);
					Node nNode = nList.item(temp);
					// obtenemos los atributos y su valor
					NamedNodeMap nodeMap = nNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node tempNode = nodeMap.item(i);
						// insert id_jugador
						int id_jugador = Integer.parseInt(tempNode.getNodeValue());
						storedProcedure.setParameter(1, id_jugador);
						// nodo = nom_jugador, insert nom_jugador
						NodeList nList2 = doc.getElementsByTagName("nom_jugador");
						Node nNode2 = nList2.item(temp);
						storedProcedure.setParameter(2, nNode2.getTextContent());
						// nodo = posicio, // insert posicio
						NodeList nList3 = doc.getElementsByTagName("posicio");
						Node nNode3 = nList3.item(temp);
						storedProcedure.setParameter(3, nNode3.getTextContent());
						// nodo = id_equip, // insert id_equip
						NodeList nList4 = doc.getElementsByTagName("id_equip");
						Node nNode4 = nList4.item(temp);
						int id_equip = Integer.parseInt(nNode4.getTextContent());
						storedProcedure.setParameter(4, id_equip);
						// nodo = nom_equip, // insert nom_equip
						NodeList nList5 = doc.getElementsByTagName("nom_equip");
						Node nNode5 = nList5.item(temp);
						storedProcedure.setParameter(5, nNode5.getTextContent());
						storedProcedure.execute();
						em.getTransaction().commit();
						System.out.println("Jugador '" + nNode2.getTextContent() + "' insertat en la taula Jugadors.");
					}
				}
			} else
				System.out.println("\nJa has insertat les dades de la taula Jugadors!");
			conn.close();
			pstmt.close();
			rs.close();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(rutaJugadors));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertsClassificacio(String rutaBdMysql) {
		String equips[] = { "Atletico", "Barcelona", "Madrid", "Sevilla", "Valencia", "Villarreal" };
		try {
			// comprobamos si hay datos
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Classificacio");
			ResultSet rs = pstmt.executeQuery();
			// si no hay los inserta
			if (rs.next() == false) {
				System.out.println();
				for (int i = 0; i < equips.length; i++) {
					emf = Persistence.createEntityManagerFactory("ManagerFutbol");
					em = emf.createEntityManager();
					storedProcedure = em.createStoredProcedureQuery("insertarClassificacio");
					try {
						em.getTransaction().begin();
						storedProcedure.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
						storedProcedure.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
						storedProcedure.registerStoredProcedureParameter(3, Integer.class, ParameterMode.IN);
						storedProcedure.registerStoredProcedureParameter(4, Integer.class, ParameterMode.IN);
						storedProcedure.registerStoredProcedureParameter(5, Integer.class, ParameterMode.IN);
						storedProcedure.registerStoredProcedureParameter(6, Integer.class, ParameterMode.IN);
						// parameters
						storedProcedure.setParameter(1, i + 1);
						storedProcedure.setParameter(2, equips[i]);
						storedProcedure.setParameter(3, 0);
						storedProcedure.setParameter(4, 0);
						storedProcedure.setParameter(5, 0);
						storedProcedure.setParameter(6, 0);
						storedProcedure.execute();
						em.getTransaction().commit();
						System.out.println("Classificacio insertada.");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						em.close();
						emf.close();
					}
				}
			} else
				System.out.println("\nJa has insertat les dades de la taula Classificacio!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mostrarClassificacio(String rutaBdMysql) {
		try {
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Classificacio ORDER BY punts desc");
			ResultSet rs = pstmt.executeQuery();
			int posicio = 1;
			System.out.println("\n---Classificacio---");
			while (rs.next()) {
				System.out.println(posicio + "\t" + rs.getString(2) + "\t" + rs.getInt(3) + "\t" + rs.getInt(4) + "\t"
						+ rs.getInt(5) + "\t" + rs.getInt(6));
				posicio++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void llegirTxt(String rutaJornada1, String rutaBdMysql) {
		String text;
		String resultats[] = new String[12];
		int j = 0;
		try {
			FileReader f = new FileReader(rutaJornada1);
			BufferedReader b = new BufferedReader(f);
			while ((text = b.readLine()) != null) {
				String[] partes = text.split(" ");
				for (int i = 0; i < partes.length; i++) {
					resultats[j] = partes[i];
					j++;
				}
			}
			f.close();
			b.close();
			insertsPartits(resultats, rutaBdMysql);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void insertsPartits(String[] resultats, String rutaBdMysql) {
		int number;
		try {
			// comprobamos si hay datos
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Partits");
			ResultSet rs = pstmt.executeQuery();
			// si no hay los inserta
			if (rs.next() == false) {
				// partit 1
				try {
					emf = Persistence.createEntityManagerFactory("ManagerFutbol");
					em = emf.createEntityManager();
					storedProcedure = em.createStoredProcedureQuery("insertarPartits");
					em.getTransaction().begin();
					storedProcedure.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(4, Integer.class, ParameterMode.IN);
					// parameters
					storedProcedure.setParameter(1, resultats[0]);
					storedProcedure.setParameter(2, number = Integer.parseInt(resultats[1]));
					storedProcedure.setParameter(3, resultats[2]);
					storedProcedure.setParameter(4, number = Integer.parseInt(resultats[3]));
					storedProcedure.execute();
					em.getTransaction().commit();
					System.out.println("Partit insertat.");

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					em.close();
					emf.close();
				}
				// partit 2
				emf = Persistence.createEntityManagerFactory("ManagerFutbol");
				em = emf.createEntityManager();
				storedProcedure = em.createStoredProcedureQuery("insertarPartits");
				try {
					em.getTransaction().begin();
					storedProcedure.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(4, Integer.class, ParameterMode.IN);
					// parameters
					storedProcedure.setParameter(1, resultats[4]);
					storedProcedure.setParameter(2, number = Integer.parseInt(resultats[5]));
					storedProcedure.setParameter(3, resultats[6]);
					storedProcedure.setParameter(4, number = Integer.parseInt(resultats[7]));
					storedProcedure.execute();
					em.getTransaction().commit();
					System.out.println("Partit insertat.");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					em.close();
					emf.close();
				}
				// partit 3
				emf = Persistence.createEntityManagerFactory("ManagerFutbol");
				em = emf.createEntityManager();
				storedProcedure = em.createStoredProcedureQuery("insertarPartits");
				try {
					em.getTransaction().begin();
					storedProcedure.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
					storedProcedure.registerStoredProcedureParameter(4, Integer.class, ParameterMode.IN);
					// parameters
					storedProcedure.setParameter(1, resultats[8]);
					storedProcedure.setParameter(2, number = Integer.parseInt(resultats[9]));
					storedProcedure.setParameter(3, resultats[10]);
					storedProcedure.setParameter(4, number = Integer.parseInt(resultats[11]));
					storedProcedure.execute();
					em.getTransaction().commit();
					System.out.println("Partit insertat.");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					em.close();
					emf.close();
				}
			} else
				System.out.println("\nJa has simulat la jornada 1!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		mostrarResultats(resultats);
	}

	public static void mostrarResultats(String resultats[]) {
		System.out.println("\n---Resultats jornada 1---");
		System.out.println(resultats[0] + " " + resultats[1] + " - " + resultats[2] + " " + resultats[3]);
		System.out.println(resultats[4] + " " + resultats[5] + " - " + resultats[6] + " " + resultats[7]);
		System.out.println(resultats[8] + " " + resultats[9] + " - " + resultats[10] + " " + resultats[11]);
	}

	public static void mostrarPartits(String rutaBdMysql) {
		try {
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			PreparedStatement pstmt = conn.prepareStatement("SELECT equip_a, gols_a, equip_b, gols_b FROM Partits");
			ResultSet rs = pstmt.executeQuery();
			System.out.println("\n---Resultats jornada 1---");
			while (rs.next()) {
				System.out.println(rs.getString(1) + " " + rs.getInt(2) + " - " + rs.getString(3) + " " + rs.getInt(4));
			}
			conn.close();
			pstmt.close();
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void actualizarClassificacio(String rutaBdMysql) {
		try {
			Connection conn = DriverManager.getConnection(rutaBdMysql, "root", "");
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Partits");
			if (rs.next()) {
				// count taula partits
				int count = rs.getInt(1);
				for (int i = 1; i < count + 1; i++) {
					PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Partits WHERE id_partit = ?");
					pstmt.setInt(1, i);
					rs = pstmt.executeQuery();
					if (rs.next()) {
						if (rs.getInt(3) > rs.getInt(5)) {
							// Equip A
							String query = "UPDATE Classificacio SET victoria = 1, punts = 3 WHERE equip = ?";
							PreparedStatement pstmt2 = conn.prepareStatement(query);
							pstmt2.setString(1, rs.getString(2));
							pstmt2.executeUpdate();
							// Equip B
							query = "UPDATE Classificacio SET derrota = 1 WHERE equip = ?";
							pstmt2 = conn.prepareStatement(query);
							pstmt2.setString(1, rs.getString(4));
							pstmt2.executeUpdate();
						} else if (rs.getInt(3) < rs.getInt(5)) {
							// Equip A
							String query = "UPDATE Classificacio SET derrota = 1 WHERE equip = ?";
							PreparedStatement pstmt2 = conn.prepareStatement(query);
							pstmt2.setString(1, rs.getString(2));
							pstmt2.executeUpdate();
							// Equip B
							query = "UPDATE Classificacio SET victoria = 1, punts = 3 WHERE equip = ?";
							pstmt2 = conn.prepareStatement(query);
							pstmt2.setString(1, rs.getString(4));
							pstmt2.executeUpdate();
						} else if (rs.getInt(3) == rs.getInt(5)) {
							// Equip A
							String query = "UPDATE Classificacio SET empat = 1, punts = 1 WHERE equip = ?";
							PreparedStatement pstmt2 = conn.prepareStatement(query);
							pstmt2.setString(1, rs.getString(2));
							pstmt2.executeUpdate();
							// Equip B
							query = "UPDATE Classificacio SET empat = 1, punts = 1 WHERE equip = ?";
							pstmt2 = conn.prepareStatement(query);
							pstmt2.setString(1, rs.getString(4));
							pstmt2.executeUpdate();
						}
					}
				}
				System.out.println("\nClassificacio actualitzada despres de la jornada 1.\n");
				try {
					conn = DriverManager.getConnection(rutaBdMysql, "root", "");
					PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Classificacio ORDER BY punts desc");
					rs = pstmt.executeQuery();
					int posicio = 1;
					while (rs.next()) {
						System.out.println(posicio + "\t" + rs.getString(2) + "\t" + rs.getInt(3) + "\t" + rs.getInt(4)
								+ "\t" + rs.getInt(5) + "\t" + rs.getInt(6));
						posicio++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				System.out.println("\nTens que simular la jornada 1 abans!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}