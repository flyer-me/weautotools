using Xunit;
using CloudWork.Model;
using CloudWork.Service;

namespace CloudWork.Test.Services
{

    public class CodeExecutionServiceTests
    {
        [Fact]
        public async Task ExecuteCodeAsync_CSharpTest()
        {
            var submission = new Submission
            {
                Id = "test_submission_1",
                Code = @"int number = Convert.ToInt32(Console.ReadLine());
                    Console.WriteLine($""CSharp answer is {number}"");",
                Language = ProgramLanguage.CSharp
            };

            var testCases = new List<TestCase>
        {
            new TestCase
            {
                Id = "test_case_1",
                Input = "42",
                ExpectedOutput = "CSharp answer is 42"
            }
        };

            var result = await CodeExecutionService.ExecuteCodeAsync(submission, testCases);
            Assert.Equal("CSharp answer is 42", result.Message);
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.True(result.IsPassed);
        }

        [Fact]
        public async Task ExecuteCodeAsync_PythonTest()
        {
            var submission = new Submission
            {
                Id = "test_submission_2",
                Code = @"
number = int(input())
print(f'Python answer is {number}')",
                Language = ProgramLanguage.Python
            };

            var testCases = new List<TestCase>
        {
            new TestCase
            {
                Id = "test_case_1",
                Input = "42",
                ExpectedOutput = "Python answer is 42"
            },
        };

            var result = await CodeExecutionService.ExecuteCodeAsync(submission, testCases);
            Assert.Equal("Python answer is 42", result.Message);
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.True(result.IsPassed);
        }

        [Fact]
        public async Task ExecuteCodeAsync_InCorrectTest()
        {
            var submission = new Submission
            {
                Id = "test_submission_3",
                Code = @"Console.WriteLine(""Incorrect Output"");",
                Language = ProgramLanguage.CSharp
            };

            var testCases = new List<TestCase>
        {
            new TestCase
            {
                Id = "test_case_1",
                Input = "0",
                ExpectedOutput = "CSharp answer is 42"
            }
        };

            var result = await CodeExecutionService.ExecuteCodeAsync(submission, testCases);

            Assert.NotEqual("CSharp answer is 42", result.Message);
            Assert.Equal("Incorrect Output", result.Message);
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.False(result.IsPassed);
        }
    }
}
