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
                Code = @"
using System;

class Program
{
    static void Main()
    {
        int number = Convert.ToInt32(Console.ReadLine());
        Console.WriteLine($""The answer is {number}"");
    }
}",
                Language = ProgramLanguage.CSharp
            };

            var testCases = new List<TestCase>
        {
            new TestCase
            {
                Id = "test_case_1",
                Input = "42",
                ExpectedOutput = "The answer is 42"
            },
            new TestCase
            {
                Id = "test_case_2",
                Input = "100",
                ExpectedOutput = "The answer is 100"
            }
        };

            var result = await CodeExecutionService.ExecuteCodeAsync(submission, testCases);
            Console.WriteLine("=================\n"+ result.Message);
            Assert.Equal("The answer is 42", result.Message);
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
print(f'The answer is {number}')
",
                Language = ProgramLanguage.Python
            };

            var testCases = new List<TestCase>
        {
            new TestCase
            {
                Id = "test_case_1",
                Input = "42",
                ExpectedOutput = "The answer is 42"
            },
            new TestCase
            {
                Id = "test_case_2",
                Input = "100",
                ExpectedOutput = "The answer is 100"
            }
        };

            var result = await CodeExecutionService.ExecuteCodeAsync(submission, testCases);
            Console.WriteLine(result.Message);
            Assert.Equal("The answer is 42", result.Message);
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.True(result.IsPassed);
        }

        [Fact]
        public async Task ExecuteCodeAsync_FailedTest()
        {
            var submission = new Submission
            {
                Id = "test_submission_3",
                Code = @"
using System;

class Program
{
    static void Main()
    {
        Console.WriteLine(""Incorrect Output"");
    }
}",
                Language = ProgramLanguage.CSharp
            };

            var testCases = new List<TestCase>
        {
            new TestCase
            {
                Id = "test_case_1",
                Input = "",
                ExpectedOutput = "The answer is 42"
            }
        };

            var result = await CodeExecutionService.ExecuteCodeAsync(submission, testCases);

            Assert.NotEqual("The answer is 42", result.Message);
            Assert.Equal("Incorrect Output", result.Message);
            Assert.True(result.ExecutionTime.TotalSeconds >= 0);
            Assert.False(result.IsPassed);
        }
    }
}
