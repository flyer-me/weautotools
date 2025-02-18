using CloudWork.Model;
using System.Diagnostics;

namespace CloudWork.Service
{
    /// <summary>
    /// C#实现JudgeService：用于执行用户提交的代码，并根据TestCase的输入和正确输出判断是否正确
    /// 输入：
    /// 输出：执行结果字符串和最短执行时间（毫秒）
    /// 运行方式：启动一个 Docker 容器，执行代码并返回结果，最大执行时间为10秒。如果超时，关闭容器并返回错误信息。
    /// 需要根据语言选择对应的 Docker 环境和文件名，对应运行环境取最新镜像。
    /// 代码字符串写入文件Program，根据语言生成对应文件，以及创建所需项目文件结构。
    /// 运行代码时，执行N次并取最短时间，返回结果和最短运行时间。
    /// </summary>
    public class CodeExecutionService
    {
        private static readonly int TimeoutSeconds = 10;
        private static readonly int REPEAT_RUN = 1;
        // private TimeSpan MaxExecutionTime = TimeSpan.FromSeconds(TimeoutSeconds);

        public static async Task<SubmissionResult> ExecuteCodeAsync(Submission submission, List<TestCase>? testCases)
        {
            string result = string.Empty;
            string image = string.Empty;
            string runCode = string.Empty;
            TimeSpan minExecutionTime = TimeSpan.MaxValue;

            var input = testCases?.First().Input ?? string.Empty;
            var output = testCases?.First().ExpectedOutput ?? string.Empty;

            var tempDirectory = Path.Combine(Path.GetTempPath(), Guid.NewGuid().ToString());
            Directory.CreateDirectory(tempDirectory);

            switch (submission.Language)
            {
                case ProgramLanguage.CSharp:
                    await SetupCSharpEnvironment(submission.Code, tempDirectory);
                    image = "mcr.microsoft.com/dotnet/sdk:9.0";
                    runCode = "dotnet run --project app/";
                    break;
                case ProgramLanguage.Python:
                    await SetupPythonEnvironment(submission.Code, tempDirectory);
                    image = "python:3.9";
                    runCode = "python program.py";
                    break;
                default:
                    throw new ArgumentException("Unsupported language");
            }

            // 取最短时间
            for (int i = 0; i < REPEAT_RUN; i++)
            {
                var (currentResult, executionTime) = await RunInDockerAsync(image, runCode, input, tempDirectory);
                if (executionTime < minExecutionTime)
                {
                    minExecutionTime = executionTime;
                    result = currentResult;
                }
            }

            return new SubmissionResult
            {
                IsPassed = string.Equals(output, result.Trim()),
                Message = result,
                ExecutionTime = minExecutionTime,
            };
        }

        private static async Task<(string result, TimeSpan executionTime)> RunInDockerAsync
            (string image, string runCode, string input, string workingDirectory)
        {
            var command = $@"docker run --rm -v ""{workingDirectory}:/app"" -w /app -ti --memory=512m --cpus=1 
--read-only 
--network none 
--security-opt=no-new-privileges:true 
--cap-drop ALL 
{image}
timeout {TimeoutSeconds} sh -c 'echo {input} | {runCode}'
--name=""CloudWork_CodeExecutionService""";

            using var process = new Process();
            process.StartInfo.FileName = "/bin/bash";
            process.StartInfo.Arguments = " -c \"" + command.Replace("\"", "\\\"").Replace("\r\n", " ").Replace("\n", " ") + "\"";
            process.StartInfo.RedirectStandardOutput = true;
            process.StartInfo.RedirectStandardError = true;
            process.StartInfo.UseShellExecute = false;
            Console.WriteLine("OJ进程输出：\n" + process.StartInfo.FileName + process.StartInfo.Arguments);
            var stopwatch = Stopwatch.StartNew();
            process.Start();

            using var cts = new CancellationTokenSource(TimeSpan.FromSeconds(TimeoutSeconds));
            try
            {
                var outputTask = process.StandardOutput.ReadToEndAsync();
                var errorTask = process.StandardError.ReadToEndAsync();

                await Task.WhenAll(outputTask, errorTask).WaitAsync(cts.Token);

                stopwatch.Stop();

                var output = await outputTask;
                var error = await errorTask;
                Console.WriteLine("OJ进程输出：\n" + output);

                if (process.HasExited)
                {
                    var exitCode = process.ExitCode;

                    if (exitCode != 0 || !string.IsNullOrEmpty(error))
                    {
                        return ($"Error: {error}", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
                    }

                    return (output.Trim(), TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
                }
                else
                {
                    process.Kill();
                    await process.WaitForExitAsync();
                    return ("Timed out", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
                }
            }
            catch (OperationCanceledException)
            {
                // Handle timeout scenario
                process.Kill();
                await process.WaitForExitAsync();
                return ("Timed out", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
            }

            /*
            var outputTask = process.StandardOutput.ReadToEndAsync();
            var errorTask = process.StandardError.ReadToEndAsync();
            await Task.WhenAll(outputTask, errorTask).WaitAsync(TimeSpan.FromSeconds(TimeoutSeconds));
            stopwatch.Stop();
            Console.WriteLine("======进程输出：\n" + outputTask.Result);
            if (!process.HasExited)
            {
                process.Kill();
                await process.WaitForExitAsync();
                return ("Timed out", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
            }

            var exitCode = process.ExitCode;
            var output = await outputTask;
            var error = await errorTask;

            if (exitCode != 0 || !string.IsNullOrEmpty(error))
            {
                return ($"Error: {error}", TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
            }

            return (output.Trim(), TimeSpan.FromMilliseconds(stopwatch.ElapsedMilliseconds));
            */
        }

        private static async Task SetupCSharpEnvironment(string code, string directory)
        {
            var programFile = Path.Combine(directory, "app", "Program.cs");

            // Create the project structure using dotnet new console
            using var createProcess = new Process();
            createProcess.StartInfo.FileName = "dotnet";
            createProcess.StartInfo.Arguments = $"new console -o app -n Program";
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
        }

        private static async Task SetupPythonEnvironment(string code, string directory)
        {
            var scriptFile = Path.Combine(directory, "program.py");
            await File.WriteAllTextAsync(scriptFile, code);
        }
    }
}
