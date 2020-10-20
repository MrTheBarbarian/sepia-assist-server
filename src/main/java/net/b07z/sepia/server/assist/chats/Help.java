package net.b07z.sepia.server.assist.chats;

import net.b07z.sepia.server.assist.answers.Answers;
import net.b07z.sepia.server.assist.assistant.ActionBuilder;
import net.b07z.sepia.server.assist.assistant.LANGUAGES;
import net.b07z.sepia.server.assist.interpreters.NluResult;
import net.b07z.sepia.server.assist.services.ServiceBuilder;
import net.b07z.sepia.server.assist.services.ServiceResult;
import net.b07z.sepia.server.core.assistant.ACTIONS;
import net.b07z.sepia.server.core.assistant.CMD;
import net.b07z.sepia.server.core.tools.Converters;
import net.b07z.sepia.server.core.tools.JSON;

/**
 * Help me! Do some helpful stuff here ^_^.<br>
 * NOTE: This "service" does not implement the ServiceInterface since it is usually called by one of 
 * the chat-services only to get the result object.
 * 
 * @author Florian Quirin
 *
 */
public class Help {
	
	/**
	 * Get a HTML version of the list of skills (topics + examples).
	 * @param language - code as usual
	 */
	public static String getSkillList(String language){
		String data = "";
		
		if (language.equals(LANGUAGES.DE)){
			data = "<div class='sepiaFW-help-view' style='padding:20px;'>"
					+ "<h2>Hier ist eine Liste von Kommandos, die du probieren kannst:</h3>"
					+ "<br>"
					+ "<h3>Wetter</h3>"
					+ "<ul>"
						+ "<li>Zeig mir das Wetter?</li>"
						+ "<li>Wie wird das Wetter Morgen?</li>"
					+ "</ul>"
					+ "<h3>Radio</h3>"
					+ "<ul>"
						+ "<li>Spiele Radio Sunshine live</li>"
						+ "<li>Starte ein Rock Radio</li>"
						+ "<li>Spiele ein Metallica Radio</li>"
						+ "<li>Spiele ein Radio mit Musik von Metallica</li>"
					+ "</ul>"
					+ "<h3>News</h3>"
					+ "<ul>"
						+ "<li>Ich würde gerne Nachrichten lesen</li>"
						+ "<li>Zeig mir die Bundesliga Tabelle</li>"
						+ "<li>Wie hat Werder Bremen gespielt?</li>"
						+ "<li>Tech News bitte</li>"
						+ "<li>Was gibt es Neues im Kino?</li>"
					+ "</ul>"
					+ "<h3>Navigation</h3>"
					+ "<ul>"
						+ "<li>Wie lange dauert es um von Köln nach Hamburg zu kommen?</li>"
						+ "<li>Bring mich von München nach Frankfurt mit Zwischenstopp Nürnberg</li>"
					+ "</ul>"
					+ "<h3>Öffentlicher Nahverkehr</h3>"
					+ "<ul>"
						+ "<li>Wie komme ich mit dem Bus nach Hause</li>"
						+ "<li>Zeig mir den Weg von München nach Berlin mit dem Zug</li>"
					+ "</ul>"
					+ "<h3>Timer/Wecker</h3>"
					+ "<ul>"
						+ "<li>Wecker stellen für morgen früh 8 Uhr</li>"
						+ "<li>Timer stellen für 15min</li>"
						+ "<li>Zeig mir meine Timer</li>"
					+ "</ul>"
					+ "<h3>Listen</h3>"
					+ "<ul>"
						+ "<li>Wäsche waschen auf meine To-Do Liste setzen</li>"
						+ "<li>Milch auf meine Einkaufsliste setzen</li>"
						+ "<li>Eine neue Liste erstellen</li>"
						+ "<li>Zeig mir die Supermarkt Liste</li>"
					+ "</ul>"
					+ "<h3>Fussball</h3>"
					+ "<ul>"
						+ "<li>Wann spielt Werder Bremen</li>"
						+ "<li>Zeig mir Bundesliga Ergebnisse</li>"
						+ "<li>Zeig mir die Bundesliga Tabelle</li>"
					+ "</ul>"
					+ "<h3>Smart Home</h3>"
					+ "<ul>"
						+ "<li>Licht im Wohnzimmer auf 70%</li>"
						+ "<li>Licht aus</li>"
						+ "<li>Status der Lampe im Badezimmer</li>"
					+ "</ul>"
					/*
					+ "<h3>Essen bestellen</h3>"
					+ "<ul>"
						+ "<li>Ich würde gerne was essen</li>"
						+ "<li>Ich habe Hunger</li>"
					+ "</ul>"
					+ "<h3>Shopping</h3>"
					+ "<ul>"
						+ "<li>Ich brauche neue Schuhe von Nike/Adidas</li>"
						+ "<li>Ich suche Sneaker in rot Größe 42 für Herren</li>"
					+ "</ul>"
					*/
					+ "<h3>Websuche</h3>"
					+ "<ul>"
						+ "<li>Google Bilder von Bonobos</li>"
						+ "<li>Schau mal bei Duck Duck Go nach Rezepten für Käsekuchen</li>"
						+ "<li>wie stehen die Apple Aktien?</li>"
					+ "</ul>"
				+ "</div>";
		}else{
			data = "<div class='sepiaFW-help-view' style='padding:20px;'>"
					+ "<h2>Here is a list of commands you can try:</h3>"
					+ "<br>"
					+ "<h3>Weather</h3>"
					+ "<ul>"
						+ "<li>How is the weather tomorrow</li>"
					+ "</ul>"
					+ "<h3>Radio</h3>"
					+ "<ul>"
						+ "<li>Play radio sunshine live</li>"
						+ "<li>Play a radio with songs of Metallica</li>"
					+ "</ul>"
					+ "<h3>News</h3>"
					+ "<ul>"
						+ "<li>I'd like to read some news</li>"
					+ "</ul>"
					+ "<h3>Navigation</h3>"
					+ "<ul>"
						+ "<li>How long does it take to get from London to Liverpool</li>"
					+ "</ul>"
					+ "<h3>Public local transport</h3>"
					+ "<ul>"
						+ "<li>Show me the way from Munich to Berlin by train</li>"
					+ "</ul>"
					+ "<h3>Timer/Alarms</h3>"
					+ "<ul>"
						+ "<li>Set an alarm for tomorrom morning 8 am</li>"
						+ "<li>Set a timer for 15min</li>"
						+ "<li>Show me my timers</li>"
					+ "</ul>"
					+ "<h3>Lists</h3>"
					+ "<ul>"
						+ "<li>Put pay bills on my to-do list</li>"
						+ "<li>Put milk on my shopping-list</li>"
						+ "<li>Create a new list</li>"
						+ "<li>Show me my supermarket list</li>"
					+ "</ul>"
					+ "<h3>Soccer</h3>"
					+ "<ul>"
						+ "<li>When does Werder Bremen play</li>"
						+ "<li>Show me Bundesliga results</li>"
						+ "<li>Show me Bundesliga standing</li>"
					+ "</ul>"
					+ "<h3>Smart Home</h3>"
					+ "<ul>"
						+ "<li>Set the light in the living room to 70%</li>"
						+ "<li>Lights off</li>"
						+ "<li>Light status in the bath</li>"
					+ "</ul>"
					/*
					+ "<h3>Food delivery</h3>"
					+ "<ul>"
						+ "<li>I'd like to eat something</li>"
					+ "</ul>"
					+ "<h3>Shopping</h3>"
					+ "<ul>"
						+ "<li>I need new shoes</li>"
					+ "</ul>"
					*/
					+ "<h3>Web search</h3>"
					+ "<ul>"
						+ "<li>Google search for Bonobos</li>"
						+ "<li>Search with Duck Duck Go for recipes of cheesecake</li>"
						+ "<li>What is the stock price of apple?</li>"
						+ "<li>Show me pictures of the most beautiful city on earth</li>"
					+ "</ul>"
				+ "</div>";
		}
		
		return data;
	}

	public static ServiceResult get(NluResult nluResult){
		//initialize result
		ServiceBuilder api = new ServiceBuilder(nluResult);
		
		//get answer
		api.answer = Answers.getAnswerString(nluResult, "chat_help_0a");
		api.answerClean = Converters.removeHTML(api.answer);
		
		//add help action
		String data = getSkillList(api.language);
			
		ActionBuilder actionBuilder = new ActionBuilder(api);
		actionBuilder.addAction(
				ACTIONS.SHOW_HTML_RESULT, 
				JSON.make("data", data), 
				JSON.make(ACTIONS.OPTION_TARGET_VIEW, "bigResults")
		);
		api.hasAction = true;
		
		api.setStatusSuccess();
		
		//anything else?
		api.context = CMD.CHAT;		//how do we handle chat contexts? Just like that and do the reset with cmd_summary?
		
		//finally build the API_Result
		ServiceResult result = api.buildResult();
		
		//return result_JSON.toJSONString();
		return result;
	}
}
