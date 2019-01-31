package net.b07z.sepia.server.assist.services;

import net.b07z.sepia.server.assist.answers.AnswerTools;
import net.b07z.sepia.server.assist.answers.Answers;
import net.b07z.sepia.server.assist.assistant.Assistant;
import net.b07z.sepia.server.assist.chats.Help;
import net.b07z.sepia.server.assist.chats.HowAreYou;
import net.b07z.sepia.server.assist.chats.WhatLanguages;
import net.b07z.sepia.server.assist.interpreters.NluResult;
import net.b07z.sepia.server.assist.interviews.NoResult;
import net.b07z.sepia.server.assist.services.ServiceInfo.Content;
import net.b07z.sepia.server.assist.services.ServiceInfo.Type;
import net.b07z.sepia.server.core.assistant.ACTIONS;
import net.b07z.sepia.server.core.assistant.CMD;
import net.b07z.sepia.server.core.assistant.PARAMETERS;
import net.b07z.sepia.server.core.tools.Debugger;

/**
 * Handles the chat command before its send to the individual chat scripts.
 * 
 * @author Florian Quirin
 *
 */
public class ChatPreprocessor implements ServiceInterface{
	
	//info
	public ServiceInfo getInfo(String language){
		return new ServiceInfo(Type.database, Content.data, true);
	}

	//result
	public ServiceResult getResult(NluResult nluResult){
		//initialize result
		ServiceBuilder api = new ServiceBuilder(nluResult);
		
		//get parameters
		String reply = nluResult.getParameter(PARAMETERS.REPLY);
		String type = nluResult.getParameter(PARAMETERS.TYPE);
		String info = nluResult.getParameter(PARAMETERS.INFO);
		Debugger.println("cmd: chat, reply=" + reply + ", type=" + type + ", info=" + info, 2);		//debug
		
		//check if there is already a reply given
		if (!reply.isEmpty()){
			//check if its direct or an link to database
			String key = AnswerTools.handleUserAnswerSets(reply);
			api.answer = Answers.getAnswerString(nluResult, key);
			api.status = "success";
			
			//TODO: influence mood?
			if (type.matches("compliment") || type.matches("apology")){
				//that makes really happy :-)
				api.mood = Assistant.mood_increase(api.mood);	api.mood = Assistant.mood_increase(api.mood);
			}
			else if (type.matches("greeting") || type.matches("thanks")){
				//that makes a bit happy :-)
				api.mood = Assistant.mood_increase(api.mood);
			}
			else if (type.matches("complain")){
				//that makes really sad :-)
				api.mood = Assistant.mood_decrease(api.mood);	api.mood = Assistant.mood_decrease(api.mood);
			}
		}
		//else link to a specific chat script if available or produce an answer
		else{
			//greeting
			if (type.matches("greeting")){
				//any greeting
				api.answer = Answers.getAnswerString(nluResult, "chat_hello_0a");
				api.status = "success";
				
				//makes a bit happy :-)
				api.mood = Assistant.mood_increase(api.mood);
			}
			
			else if (type.matches("goodbye")){
				//any greeting
				api.answer = Answers.getAnswerString(nluResult, "chat_goodbye_0a");
				api.status = "success";
				
				//makes a bit sad
				//api.mood = Assistant.mood_decrease(api.mood);
			}
			
			//help!
			else if (type.matches("help")){
				//help API
				return Help.get(nluResult);
			}
			
			//questions
			else if (type.matches("question")){
				
				//assumer success - set different individually
				api.status = "success";
				
				//how are you?
				if (info.matches("how_are_you")){
					return HowAreYou.get(nluResult);
				
				//languages?
				}else if (info.matches("languages")){
					return WhatLanguages.get(nluResult);
				
				//skills?
				}else if (info.matches("skills")){
					//add help action
					String data = Help.getSkillList(api.language);
						
					api.addAction(ACTIONS.SHOW_HTML_RESULT);
					api.putActionInfo("data", data);
					api.hasAction = true;
					
					api.answer = Answers.getAnswerString(nluResult, "chat_skills_0a");
					
				//name?
				}else if (info.matches("name")){
					api.answer = Answers.getAnswerString(nluResult, "chat_name_0a");
					
				//meaning of life?
				}else if (info.matches("meaning_of_life")){
					api.answer = Answers.getAnswerString(nluResult, "chat_meaning_of_life_0a");
					
				//origin?
				}else if (info.matches("origin")){
					api.answer = Answers.getAnswerString(nluResult, "chat_origin_0a");
				
				//age?
				}else if (info.matches("age")){
					api.answer = Answers.getAnswerString(nluResult, "chat_age_0a");
					
				//creator?
				}else if (info.matches("creator")){
					api.answer = Answers.getAnswerString(nluResult, "chat_creator_0a");
					
				//kind of creation
				}else if (info.matches("kind_of_creation")){
					api.answer = Answers.getAnswerString(nluResult, "chat_kind_of_creation_0a");
				
				//Daniel's additions
				}else if (info.matches("humans_only")){
					api.answer = Answers.getAnswerString(nluResult, "chat_humans_only_0a");
				}else if (info.matches("real")){
					api.answer = Answers.getAnswerString(nluResult, "chat_real_0a");
				}else if (info.matches("rhetorical")){
					api.answer = Answers.getAnswerString(nluResult, "chat_rhetorical_0a");
				}else if (info.matches("deny")){
					api.answer = Answers.getAnswerString(nluResult, "chat_deny_0a");
				}else if (info.matches("programmed")){
					api.answer = Answers.getAnswerString(nluResult, "chat_programmed_0a");
				}else if (info.matches("nobody_knows")){
					api.answer = Answers.getAnswerString(nluResult, "chat_nobody_knows_0a");
				}else if (info.matches("news")){
					api.answer = Answers.getAnswerString(nluResult, "chat_news_0a");
				}else if (info.matches("with_pleasure")){
					api.answer = Answers.getAnswerString(nluResult, "chat_with_pleasure_0a");
				}else if (info.matches("no_eat")){
					api.answer = Answers.getAnswerString(nluResult, "chat_no_eat_0a");
				}else if (info.matches("impossible_command")){
					api.answer = Answers.getAnswerString(nluResult, "chat_impossible_command_0a");
				}else if (info.matches("whenever_you_want")){
					api.answer = Answers.getAnswerString(nluResult, "chat_whenever_you_want_0a");
				
				//unknown question
				}else{
					return NoResult.get(nluResult);
				}
			}
			
			//compliments
			else if (type.matches("compliment")){
				//any compliment
				api.answer = Answers.getAnswerString(nluResult, "chat_compliment_0a");
				api.status = "success";
				
				//compliments make happy :-)
				api.mood = Assistant.mood_increase(api.mood);
				api.mood = Assistant.mood_increase(api.mood);	//twice ;-)
			}
			
			//complaints
			else if (type.matches("complain")){
				//any complain
				api.answer = Answers.getAnswerString(nluResult, "chat_complain_0a");
				api.status = "success";
				
				//complaints make sad :-)
				api.mood = Assistant.mood_decrease(api.mood);
				api.mood = Assistant.mood_decrease(api.mood);	//twice ;-)
			}
			
			//thanks
			else if (type.matches("thanks")){
				//any thanks
				api.answer = Answers.getAnswerString(nluResult, "chat_thanks_0a");
				api.status = "success";
				
				//makes happier :-)
				api.mood = Assistant.mood_increase(api.mood);
			}
			
			//apology
			else if (type.matches("apology")){
				//any apology
				api.answer = Answers.getAnswerString(nluResult, "chat_apology_0a");
				api.status = "success";
				
				//makes happier :-)
				api.mood = Assistant.mood_increase(api.mood);
				api.mood = Assistant.mood_decrease(api.mood);	//twice ;-)
			}
			
			//game_cool 8-)
			else if (type.matches("game_cool")){
				//any thanks
				api.answer = Answers.getAnswerString(nluResult, "chat_game_cool");
				api.status = "success";
				
				//makes happier :-)
				api.mood = Assistant.mood_increase(api.mood);
			}
			
			//no result
			else{
				return NoResult.get(nluResult);
			}
		}
		
		//finish:
		
		//get clean answer - removed to use default "cleaner" from "buildResult()"
		//api.answerClean = Converters.removeHTML(api.answer);
		
		//anything else?
		api.context = CMD.CHAT;				//how do we handle chat contexts? Just like that and do the rest with cmd_summary?
		
		//finally build the API_Result
		ServiceResult result = api.buildResult();
				
		//return result_JSON.toJSONString();
		return result;
	}

}
