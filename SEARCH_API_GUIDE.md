# 🔍 Search API Guide

This guide explains how to use the search functionality in the Purchase Post Service backend.

## Search Endpoint

### `GET /api/posts/search`

Search posts by keyword across multiple fields including post text, author information, and platform names.

#### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `keyword` | String | No | - | Search keyword (minimum 2 characters) |
| `page` | Integer | No | 0 | Page number (0-based) |
| `size` | Integer | No | 10 | Number of results per page |

#### Search Fields

The search functionality searches across the following fields:
- **Post text**: The main content of the post
- **Author username**: Username of the post author
- **Author first name**: First name of the post author  
- **Author last name**: Last name of the post author
- **Platform name**: Name of the platform (Amazon, eBay, etc.)

#### Response Format

```json
{
  "posts": [
    {
      "id": "uuid",
      "text": "Post content...",
      "author": {
        "id": "uuid",
        "username": "johndoe",
        "firstName": "John",
        "lastName": "Doe"
      },
      "platform": {
        "id": "uuid",
        "name": "Amazon"
      },
      "likeCount": 10,
      "commentCount": 5,
      "repostCount": 2,
      "shareCount": 3,
      "createdAt": "2023-12-01T10:00:00Z"
    }
  ],
  "total": 25,
  "page": 0,
  "size": 10,
  "hasNext": true,
  "keyword": "search term"
}
```

#### Error Response

When keyword is less than 2 characters:

```json
{
  "error": "Search keyword must be at least 2 characters long",
  "posts": [],
  "total": 0,
  "page": 0,
  "size": 10,
  "hasNext": false
}
```

## Usage Examples

### 1. Basic Search
```bash
GET /api/posts/search?keyword=amazon
```
Finds all posts containing "amazon" in any searchable field.

### 2. Author Search  
```bash
GET /api/posts/search?keyword=johndoe
```
Finds posts by users with "johndoe" in their username, first name, or last name.

### 3. Multi-word Search
```bash
GET /api/posts/search?keyword=great%20deal
```
Finds posts where both "great" AND "deal" appear in the searchable fields.

### 4. Paginated Search
```bash
GET /api/posts/search?keyword=laptop&page=1&size=5
```
Search for "laptop" with pagination (page 1, 5 results per page).

### 5. Empty Search
```bash
GET /api/posts/search
```
Returns all posts (same as `/api/posts` endpoint).

## Search Features

### Case Insensitive
All searches are case-insensitive. "AMAZON", "amazon", and "Amazon" will return the same results.

### Partial Matching
The search uses partial matching with wildcards. Searching for "lap" will match "laptop", "overlap", etc.

### Multi-keyword Support
- **Single keyword**: Searches for the keyword in all fields
- **Two keywords**: Both keywords must be found (AND logic)
- **More than two keywords**: Uses only the first keyword (can be enhanced later)

### Ordering
Results are ordered by creation date (newest first).

## Frontend Integration

### JavaScript/React Example
```javascript
const searchPosts = async (keyword, page = 0, size = 10) => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });
  
  if (keyword && keyword.trim().length >= 2) {
    params.append('keyword', keyword.trim());
  }
  
  const response = await fetch(`/api/posts/search?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  return response.json();
};

// Usage
const results = await searchPosts('amazing deal', 0, 20);
console.log(`Found ${results.total} posts`);
```

### cURL Examples
```bash
# Basic search
curl -X GET "http://localhost:8081/api/posts/search?keyword=laptop"

# Search with pagination
curl -X GET "http://localhost:8081/api/posts/search?keyword=gaming&page=0&size=5"

# Search for author
curl -X GET "http://localhost:8081/api/posts/search?keyword=john"
```

## Performance Notes

- The search uses database indexes for optimal performance
- Large datasets might benefit from additional indexing on searchable fields
- Consider implementing full-text search (like Elasticsearch) for advanced search features

## Future Enhancements

The current implementation can be extended with:
- **Advanced filters**: Price range, date range, platform filtering
- **Full-text search**: Integration with Elasticsearch or similar
- **Search suggestions**: Auto-complete functionality
- **Search analytics**: Track popular search terms
- **Fuzzy matching**: Handle typos and similar spellings
- **Faceted search**: Category-based filtering