using Xunit;
using Newtonsoft.Json;
using CloudWork.Model;
using CloudWork.Service;
using Newtonsoft.Json.Linq;

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

            var result = await CodeExecutionService.JuidgeAsync(submission, testCases);
            JObject jsonObj = JObject.Parse(result.Message);
            Assert.Equal("CSharp answer is 42", jsonObj["Results"]?[0]?["Actual"]?.ToString());
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.True(result.IsPassed);
            Assert.False(result.CompileError);
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

            var result = await CodeExecutionService.JuidgeAsync(submission, testCases);
            JObject jsonObj = JObject.Parse(result.Message);
            Assert.Equal("Python answer is 42", jsonObj["Results"]?[0]?["Actual"]?.ToString());
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.True(result.IsPassed);
            Assert.False(result.CompileError);
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

            var result = await CodeExecutionService.JuidgeAsync(submission, testCases);

            JObject jsonObj = JObject.Parse(result.Message);
            Assert.Equal("Incorrect Output", jsonObj["Results"]?[0]?["Actual"]?.ToString());

            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.False(result.IsPassed);
            Assert.False(result.CompileError);
        }

        [Fact]
        public async Task ExecuteCodeAsync_CompileErrorTest()
        {
            var submission = new Submission
            {
                Id = "test_submission_3",
                Code = @"Run()",
                Language = ProgramLanguage.CSharp
            };

            var testCases = new List<TestCase>
        {
            new() {
                Id = "test_case_1",
                Input = "0",
                ExpectedOutput = "CSharp answer is 42"
            }
        };

            var result = await CodeExecutionService.JuidgeAsync(submission, testCases);
            Assert.False(result.IsPassed);
            Assert.True(result.CompileError);
        }
    }
}
