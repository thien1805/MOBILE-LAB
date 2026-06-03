package com.example.lab04inclass

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab04inclass.ui.theme.Lab04InClassTheme

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database helper
        dbHelper = DatabaseHelper(this)
        
        enableEdgeToEdge()
        setContent {
            Lab04InClassTheme {
                DictionaryApp(dbHelper)
            }
        }
    }
}

@Composable
fun DictionaryApp(dbHelper: DatabaseHelper) {
    var searchInput by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var substringMatches by remember { mutableStateOf(listOf<String>()) }
    var resultMessage by remember { mutableStateOf("") }
    var searchPerformed by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Dictionary App",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Input Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    label = { Text("Enter a word") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        if (searchInput.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please enter a word to search",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            performSearch(
                                searchInput,
                                dbHelper,
                                onDefinitionFound = { def -> definition = def },
                                onSubstringMatches = { matches -> substringMatches = matches },
                                onResultMessage = { message -> resultMessage = message },
                                onSearchPerformed = { searchPerformed = true }
                            )
                        }
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .width(100.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LOOKUP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Result Display Area
            if (searchPerformed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Definition Result Section
                    if (definition.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF0F7FF)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Definition:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F41BB)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = definition,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Result Message Section
                    if (resultMessage.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF4E6)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = resultMessage,
                                fontSize = 14.sp,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Substring Matches Section
                    if (substringMatches.isNotEmpty()) {
                        Text(
                            text = "Similar Words (${substringMatches.size}):",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(substringMatches) { word ->
                                Text(
                                    text = "• $word",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Perform the dictionary search logic:
 * 1. Check for exact match
 * 2. If not found, check for substring matches
 * 3. Update results accordingly
 */
private fun performSearch(
    searchTerm: String,
    dbHelper: DatabaseHelper,
    onDefinitionFound: (String) -> Unit,
    onSubstringMatches: (List<String>) -> Unit,
    onResultMessage: (String) -> Unit,
    onSearchPerformed: () -> Unit
) {
    // Reset previous results
    onDefinitionFound("")
    onSubstringMatches(emptyList())
    onResultMessage("")

    // Step 1: Check for exact match (case-insensitive)
    val exactMatch = dbHelper.searchExactMatch(searchTerm.lowercase())

    if (exactMatch != null) {
        // Found exact match - display definition
        onDefinitionFound(exactMatch)
    } else {
        // Step 2: Check for substring matches
        val matches = dbHelper.searchSubstringMatches(searchTerm.lowercase())

        if (matches.isNotEmpty()) {
            // Found substring matches - display list
            onSubstringMatches(matches)
            onResultMessage("No exact match found. Showing similar words:")
        } else {
            // Step 3: No matches found
            onResultMessage("Word not found in dictionary.")
        }
    }

    onSearchPerformed()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Lab04InClassTheme {
    }
}