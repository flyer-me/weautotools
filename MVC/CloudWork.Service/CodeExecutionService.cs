using CloudWork.Model;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Text.Json;
using System.Text.RegularExpressions;

namespace CloudWork.Service
{
    /// <summary>
    /// 执行Submission，并根据TestCases的输入和正确输出判断是否正确
    /// </summary>
    public class CodeExecutionService
    {
        private static readonly int TimeoutSeconds = 15;
        private static readonly int REPEAT_RUN = 1;
        private static readonly bool CLEAN_FILES = false;

        public static async Task<SubmissionResult> JuidgeAsync(Submission submission, List<TestCase> testCases)
        {
            var EnvConfig = (Image: string.Empty, CompileCommand: string.Empty, RunCommand: string.Empty);  // 运行环境配置

            var tempDirectory = Path.Combine("/mnt/oj/submission", Guid.NewGuid().ToString());
            Directory.CreateDirectory(tempDirectory);

            if (testCases?.Count > 0)   // 生成1.input, 2.input, ... 的输入文件
            {
                int index = 0;
                var tasks = new List<Task>();
                foreach (var testCase in testCases)
                {
                    tasks.Add(File.WriteAllTextAsync(Path.Combine(tempDirectory, $"{index}.input"), testCase.Input));
                    tasks.Add(File.WriteAllTextAsync(Path.Combine(tempDirectory, $"{index}.output"), testCase.ExpectedOutput));
                    index++;
                }
                await Task.WhenAll(tasks);
            }

            switch (submission.Language)    // 获取环境配置
            {
                case ProgramLanguage.CSharp:
                    EnvConfig = await SetupCSharpEnvironment(submission.Code, tempDirectory);
                    break;
                case ProgramLanguage.Python:
                    EnvConfig = await SetupPythonEnvironment(submission.Code, tempDirectory);
                    break;
                default:
                    throw new ArgumentException("Unsupported language");
            }

            var containerId = await StartContainer(tempDirectory, EnvConfig.Image, EnvConfig.RunCommand);
            if (!string.IsNullOrWhiteSpace(EnvConfig.CompileCommand))
            {
                var compileResult = await Compile(containerId, EnvConfig.CompileCommand);
                if (compileResult.StartsWith("Error"))
                {
                    await CleanEnvironment(containerId);
                    return new SubmissionResult
                    {
                        IsPassed = false,
                        CompileError = true,
                        Message = compileResult,
                        ExecutionTime = TimeSpan.Zero,
                    };
                }
            }

            TimeSpan minExecutionTime = TimeSpan.MaxValue;
            string result = string.Empty;
            for (int i = 0; i < REPEAT_RUN; i++)
            {
                var (currentResult, executionTime) = await Run(containerId, EnvConfig.RunCommand);
                if (executionTime < minExecutionTime)
                {
                    minExecutionTime = executionTime;
                    result = currentResult;
                }
            }
            await CleanEnvironment(containerId);

            /* 过滤dotnet运行时输出
            result = Regex.Replace(result, @"^\x1B\x5B\x3F\x31\x68\x1B\x3D\x1B\x5B\x3F\x31\x68\x1B\x3D", string.Empty);
            string answers = testCases?.Aggregate(new StringBuilder(), (sb, test) => sb.Append(test.ExpectedOutput))
                .ToString() ?? string.Empty;
            */
            bool isPassed = true;
            if (!result.StartsWith('{'))    // 如果不是json格式，说明有错误
            {
                return new SubmissionResult
                {
                    IsPassed = false,
                    CompileError = false,
                    Message = "Error: " + result,
                    ExecutionTime = minExecutionTime,
                };
            }

            Result testResult = JsonSerializer.Deserialize<Result>(result) ?? new Result();

            if (testResult != null && testResult.Results != null)
            {
                foreach (var testCase in testResult.Results)
                {
                    if (testCase.Status != "AC")
                    {
                        isPassed = false;
                        break;
                    }
                }
            }
            else
            {
                isPassed = false;
            }
            return new SubmissionResult
            {
                IsPassed = isPassed,
                CompileError = false,
                Message = result,
                ExecutionTime = minExecutionTime,
            };
        }

        private static async Task<string> StartContainer(string directory, string image, string runCode)
        {
            var command = $@"/mnt/oj/shell/startcontainer.sh ""{directory}"" ""{image}""";
            var (output, _) = await ExecuteCommand(command);
            return output;
        }

        private static async Task<string> Compile(string containerId, string compileCode)
        {
            var command = $@"/mnt/oj/shell/compile.sh ""{containerId}"" ""{compileCode}""";
            var (output, _) = await ExecuteCommand(command);
            return output;
        }

        private static async Task<(string output, TimeSpan time)> Run(string containerId, string runCode)
        {
            var command = $@"/mnt/oj/shell/runtests.sh ""{containerId}"" ""{runCode}""";

            var result = await ExecuteCommand(command);
            return result;
        }

        private static async Task CleanEnvironment(string containerId)
        {
            var command = $"/mnt/oj/shell/cleancontainer.sh {containerId}";
            if (CLEAN_FILES)
            {
                // command += $"rm -rf {tempDirectory}";
            }
            await ExecuteCommand(command);
        }

        private static async Task<(string output, TimeSpan executionTime)> ExecuteCommand(string command)
        {
            using var process = new Process();
            process.StartInfo.FileName = "/bin/bash";
            // process.StartInfo.Arguments = " -c \"" + command.Replace("\"", "\\\"").Replace("\r\n", " ").Replace("\n", " ") + "\"";
            process.StartInfo.Arguments = command;
            process.StartInfo.RedirectStandardOutput = true;
            process.StartInfo.RedirectStandardError = true;
            process.StartInfo.UseShellExecute = false;
            // Console.WriteLine("进程启动 " + process.StartInfo.FileName + process.StartInfo.Arguments);
            var stopwatch = Stopwatch.StartNew();
            process.Start();

            using var cts = new CancellationTokenSource(TimeSpan.FromSeconds(TimeoutSeconds));
            try
            {
                var outputTask = process.StandardOutput.ReadToEndAsync();
                var errorTask = process.StandardError.ReadToEndAsync();

                await Task.WhenAll(outputTask, errorTask).WaitAsync(cts.Token);

                stopwatch.Stop();

                var output = (await outputTask).Trim();
                var error = await errorTask;

                if (process.HasExited)
                {
                    var exitCode = process.ExitCode;
                    // Console.WriteLine("进程输出 " + output);
                    if (exitCode != 0 || !string.IsNullOrEmpty(error))
                    {
                        // Console.WriteLine("进程错误 " + error);
                        return ($"Error: {error}", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
                    }
                    return (output, TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
                }
                else
                {
                    process.Kill();
                    await process.WaitForExitAsync();
                    return ("Error: Timed out", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
                }
            }
            catch (OperationCanceledException)
            {
                process.Kill();
                await process.WaitForExitAsync();
                return ("Error: Timed out", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
            }
        }

        /// <summary>
        /// 创建C#运行环境，提供镜像、编译命令和运行命令
        /// </summary>
        /// <param name="code"></param>
        /// <param name="directory"></param>
        /// <returns></returns>
        /// <exception cref="Exception"></exception>
        private static async Task<(string image, string compile, string command)> SetupCSharpEnvironment(string code, string directory)
        {
            var programFile = Path.Combine(directory, "Program.cs");
            using var createProcess = new Process();
            createProcess.StartInfo.FileName = "dotnet";
            createProcess.StartInfo.Arguments = "new console -o . -n Program";
            createProcess.StartInfo.WorkingDirectory = directory;
            createProcess.StartInfo.RedirectStandardOutput = true;
            createProcess.StartInfo.RedirectStandardError = true;
            createProcess.StartInfo.UseShellExecute = false;

            createProcess.Start();
            await createProcess.WaitForExitAsync();

            if (createProcess.ExitCode != 0)
            {
                var error = await createProcess.StandardError.ReadToEndAsync();
                throw new Exception($"Failed to create C# project: {error}");
            }

            // 替换文件
            await File.WriteAllTextAsync(programFile, code);
            return ("mcr.microsoft.com/dotnet/sdk:9.0", $"dotnet publish -o . --os linux > /dev/null 2>&1", $"dotnet Program.dll");
        }

        /// <summary>
        /// 创建Python运行环境，提供镜像、编译命令（无）和运行命令
        /// </summary>
        /// <param name="code"></param>
        /// <param name="directory"></param>
        /// <returns></returns>
        private static async Task<(string image, string compile, string command)> SetupPythonEnvironment(string code, string directory)
        {
            var scriptFile = Path.Combine(directory, "Program.py");
            await File.WriteAllTextAsync(scriptFile, code);
            return ("python:3.9", string.Empty, "python Program.py");
        }
    }

    class TestCaseResult
    {
        public string Case_id { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
        public string Code { get; set; } = string.Empty;
        public string Actual { get; set; } = string.Empty;
        public string Expect { get; set; } = string.Empty;
        public string Error { get; set; } = string.Empty;
    }

    class Result
    {
        public List<TestCaseResult> Results { get; set; } = [];
    }
}

