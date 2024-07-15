package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.ChatHistory
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.conversation
import java.util.regex.Pattern

class AssistantModeFunctions(private val context: Context): KoinComponent {
     // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    val openAIViewModel: OpenAIViewModel by inject()

//    suspend fun ask_ai(question: List<ChatHistory.Message>): String {
//        // Ask the AI a question
//        val intermediateResponse = convert_chat_to_functions(conversation=question)
//        val parsedResponse = parseAssistantOutput(intermediateResponse)
//
//        return openAIViewModel.getChatGPTResponse("I am your AI Assistant. How can I help you?", question)
//    }
     fun getCurrentTimeFormatted(): String {
        val now = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault())
        return formatter.format(now)
    }
    suspend fun convert_chat_to_functions(conversation: List<ChatHistory.Message>): String{
        val currentDateTime = getCurrentTimeFormatted()
        val output =  openAIViewModel.getChatGPTResponse(
                "You are a helpful personal assistant. You are a productivity focused assistant, with access to the following functions:  \n" +
                "1. create_reminder(string reminderText, long dateAndTimeForReminder): This will create a reminder with the given text at the given time. \n" +
                "2. create_unscheduled_reminder(string reminderText): This will create an unscheduled reminder. \n" +
                "3. respond_to_user(string responseText): This will send the response back to the user. Use it to ask for details or tell the user what you did.  \n" +
                "4. get_note_titles(): Gets a note title. [Will return a string value]  \n" +
                "5. add_to_note(string noteText, string noteTitle): Adds the noteText to the note with title noteTitle.  \n" +
                "6. ask_ai(string prompt): Use this to ask an AI Agent to perform a task. [Will return a string value]  \n" +
                "7. summarize_text(): Give a summary of the text which can be used for notes or general description of tasks and events. [ returns a string value] \n" +
                "8. get_contact(string name): Search for contact details and get a number and email. \n" +
                "9. create_calendar_event(string text, string description, long startDateTime, long endDateTime): This will add a calendar event to the users calendar using the details provided. \n" +
                "10. save_contact(string name, string email, string number): This stores the contacts details mentioned in user's device. \n" +
                "11. get_available_times(string day, string currentDateTime): This gives a list of available times that the user is free on the specific day mentioned [returns an object with startDatetime and endDatetime] \n" +
                "Your job is to output a series of function calls from the functions given above to help the user complete their task. \n" +
                "Remember, you can only help with productivity related tasks. \n" +
                "If the user does not give any indication of what they wanted you to do, you can assume they wanted to add it to their notes. \n" +
                "The current time is "+ currentDateTime+", you can use this time as reference where you have to calculate the time required and make sure to keep all the datetime format the same.\n" +
                "Use your best judgement to decide if adding a reminder or adding to notes would be better. Don't produce any comments in output. \n" +
                "If any small calculations are required you can use ask_ai function to make a call to complete the task. \n" +
                "Don't use any calculations as functions parameter in function call.If the user input implies an action that is not time-specific, create an unscheduled reminder. For example: \n" +
                "User Input: Have to check tasks created from today’s transcripts \n" +
                "Your Output: \n" +
                "\$1 = create_unscheduled_reminder(“Check tasks created from today’s transcripts”) \n" +
                "respond_to_user(“Got it, I’ve added an unscheduled reminder to check tasks created from today’s transcripts.”) \n" +
                "Examples: User Input: Remember to submit the project proposal for the software engineering class by Friday.  \n" +
                "Your Output: \$1 = get_available_times(string \"Friday\", <current datetime> ) create_reminder(“Submit Project Proposal for software engineering class”, “\$1”) respond_to_user(“Got it, I’ve added a reminder.”)  \n" +
                "User Input: Aws login > S3 > Connect IAM Role \n" +
                " Your Output: \$1 = get_note_titles()  \n" +
                "\$2 = ask_ai(“Here is a piece of text the user wants to add to their notes: \$prompt. From the following notes, give me the name of the note this text would belong to: \$1. To indicate creation of a new note, respond with the name of the suggested new note. Only create a note when absolutely needed.”)  \n" +
                "add_to_note(“\$userInput”, \$2)  \n" +
                "respond_to_user(“I’m adding this to \$2”) \n" +
                "Note, you can refer to the output of certain functions by using the \$<varNum> notation, which will be an int like \$1, \$2 and so on and the original user input using \$userInput. \n" +
                "Question: it is the prompt that we will pass\n" +
                "Answer: you have to give\n"
                , conversation.last().toString(), modelToUse = "gpt-4o")
        // TODO: Add entire conversation to prompt
        if (output != null){
            return output

        }
        return null.toString()
    }
    fun createReminder(reminderText: String, dateAndTimeForReminder: String): String {
        return "Reminder created for '$reminderText' at $dateAndTimeForReminder"
    }

    fun createUnscheduledReminder(reminderText: String): String {
        return "Unscheduled reminder created for '$reminderText'"
    }

    fun respondToUser(responseText: String): String {
        return responseText
    }

    fun getNoteTitles(): List<String> {
        return listOf("Meeting Notes", "Project Ideas", "Todo List")
    }

    fun addToNote(noteText: String, noteTitle: String): String {
        return "Added to '$noteTitle': $noteText"
    }

    suspend fun askAi(prompt: String): String {
        return openAIViewModel.getChatGPTResponse("You are given the task to perform only respond with the exact required output. Don't use any formatting", prompt, modelToUse = "gpt-3.5-turbo-instruct")
    }
    fun summarizeText(): String {
        return "Summary of the text"
    }

    fun getContact(name: String): String {
        return "Contact details for $name: 123-456-7890"
    }

    fun createCalendarEvent(text: String, description: String, startDateTime: String, endDateTime: String): String {
        return "Calendar event '$text' created with description '$description' from $startDateTime to $endDateTime"
    }

    fun saveContact(name: String, email: String, phone: String): String {
        return "Contact saved: $name, Email: $email, Phone: $phone"
    }

    fun getAvailableTimes(day: String, currentDateTime: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault()).parse(currentDateTime)
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        return "Available times on $day: $formattedDate"
    }

    fun getFunctionType(input: String): EnumFunctionTypes {
        return when (input) {
            "get_available_times" -> EnumFunctionTypes.GET_AVAILABLE_TIMES
            "create_reminder" -> EnumFunctionTypes.CREATE_REMINDER
            "create_unscheduled_reminder" -> EnumFunctionTypes.CREATE_UNSCHEDULED_REMINDER
            "get_note_titles" -> EnumFunctionTypes.GET_NOTE_TITLES
            "add_to_note" -> EnumFunctionTypes.ADD_TO_NOTES
            "respond_to_user" -> EnumFunctionTypes.RESPOND_TO_USER
            "create_calendar_event" -> EnumFunctionTypes.CREATE_EVENT
            "summarize_text" -> EnumFunctionTypes.SUMMARIZE_TEXT
            "ask_ai" -> EnumFunctionTypes.ASK_AI
            "get_contact" -> EnumFunctionTypes.GET_CONTACT
            "save_contact" -> EnumFunctionTypes.SAVE_CONTACT
            // "convert_chat_to_function" -> EnumFunctionTypes.CONVERT_CHAT_TO_FUNCTION
            else -> throw IllegalArgumentException("Unknown function name: $input")
        }

    }

    suspend fun parseAssistantOutput(inputText: String): List<FunctionsWithParams> {
        val tasks = mutableListOf<FunctionsWithParams>()
        val lines = inputText.trim().split("\n")

        val functionPattern = Pattern.compile("\\$(\\d+)\\s*=\\s*(\\w+)\\((.*)\\)")
        val responsePattern = Pattern.compile("\\s*(\\w+)\\((.*)\\)")

        for (line in lines) {

            var matcher_1 = functionPattern.matcher(line)
            var matcher_2 = responsePattern.matcher(line)
            if (matcher_1.find()) {
                val varNumber = matcher_1.group(1)
                val funcName = matcher_1.group(2)
                val args = matcher_1.group(3)

                val argsList = args.split(",").map { it.trim().trim('"') }
                val paramsMap = argsList.mapIndexed { index, arg -> "arg$index" to arg }.toMap()

                val functionType = getFunctionType(input = funcName)

                tasks.add(FunctionsWithParams(
                    function = functionType,
                    params = paramsMap,
                    varNumberToStoreIn = "\$$varNumber"
                ))
            }
            else if (matcher_2.find()) {
//                val varNumber = matcher_2.group(1)
                val funcName = matcher_2.group(1)
                val args = matcher_2.group(2)

                val argsList = args.split(",").map { it.trim().trim('"') }
                val paramsMap = argsList.mapIndexed { index, arg -> "arg$index" to arg }.toMap()

                val functionType = getFunctionType(input = funcName)

                tasks.add(FunctionsWithParams(
                    function = functionType,
                    params = paramsMap,
                    varNumberToStoreIn = null
                ))
            }
//            else {
//                matcher = responsePattern.matcher(line)
//                if (matcher.find()) {
//                    val response = matcher.group(1).trim().trim('"')
//                    tasks.add(FunctionsWithParams(EnumFunctionTypes.RESPOND_TO_USER, mapOf("response" to response)))
//                }
//            }
        }

        return tasks
    }



//    fun getFunctionsList()
//    suspend fun parse_ai_response(rawResponse: String): List<FunctionsWithParams> {
//        // Parse the AI's response
//
//        // return rawResponse.split("\n")
//        return listOf(
////            FunctionsWithParams(EnumFunctionTypes.ADD_TO_NOTES, mapOf("note" to "This is a note")),
////            FunctionsWithParams(EnumFunctionTypes.SEND_EMAIL, mapOf("email" to "This is an email")),
////            FunctionsWithParams(EnumFunctionTypes.RESPOND_TO_USER, mapOf("response" to "This is a response")),
////            FunctionsWithParams(EnumFunctionTypes.CONVERT_CHAT_TO_FUNCTION, mapOf("conversation" to "This is a conversation"))
//        )
//    }
}
//    suspend fun addToNotes(noteText: String?) {
//        // Add the user's request to the notes
//    }

//    suspend fun sendEmail(emailText: String?) {
//        // Send an email to the user
//    }




data class FunctionsWithParams(
    val function: EnumFunctionTypes,
    val params: Map<String, String>,
    val varNumberToStoreIn: String? = null,
)

enum class EnumFunctionTypes {
//     CONVERT_CHAT_TO_FUNCTION,
    ADD_TO_NOTES,
//    SEND_EMAIL,
    RESPOND_TO_USER,
    CREATE_REMINDER,
    CREATE_UNSCHEDULED_REMINDER,
    GET_NOTE_TITLES,
    GET_AVAILABLE_TIMES,
    CREATE_EVENT,
    SUMMARIZE_TEXT,
    ASK_AI,
    GET_CONTACT,
    SAVE_CONTACT
}