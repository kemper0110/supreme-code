# User

- getReferenceById(id: Long): User
- findById(id: Long): User
- findAll(): List<User>
- findByUsername(username: String): User
- save(user: User): User
- deleteById(id: Long): void
- saveAvatar(avatar: MultipartFile): String
- replaceAvatar(name: String, avatar: MultipartFile): String
- deleteAvatar(name: String): void

# Problem

- getReferenceById(id: Long): Problem
- findById(id: Long): Problem
- save(problem: Problem): Problem
- deleteById(id: Long): void
- findFiltered(String name, String difficulty, List<Long> languages, List<Long> tags): List<Problem>

# Tag

- getReferenceById(id: Long): Tag
- findById(id: Long): Tag
- findAll(): List<Tag>
- save(tag: Tag): Tag
- deleteById(id: Long): void

# Language

- getReferenceById(id: Long): Language
- findById(id: Long): Language
- findAll(): List<Language>
- save(language: Language): Language
- deleteById(id: Long): void

# ProblemLanguage

- getReferenceById(id: Long): ProblemLanguage
- findById(id: Long): ProblemLanguage
- findAll(): List<ProblemLanguage>
- save(problemLanguage: ProblemLanguage): ProblemLanguage
- deleteById(id: Long): void

# Solution

- getReferenceById(id: Long): Solution
- findById(id: Long): Solution
- findByUserId(userId: Long): Solution
- findByUserIdAndLanguageId(userId: Long, languageId: Language): Solution
- findAll(): List<Solution>
- save(solution: Solution): Solution
- deleteById(id: Long): void

# SolutionResult

- getReferenceById(id: Long): SolutionResult
- findById(id: Long): SolutionResult
- save(solutionResult: SolutionResult): SolutionResult
- deleteById(id: Long): void
- findByProblemIdAndUserIdOrderByCreatedAtDesc(Long problemId, Long userId): List<SolutionResult>

## TestListJsonConverter

- convertToDatabaseColumn(attribute: List<TestResult>): String
- convertToEntityAttribute(dbData: String): List<TestResult>


